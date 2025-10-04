package com.movie.bookMyShow.service;

import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.SeatAlreadyBookedException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SeatHoldService {
    private static final String HOLD_KEY_PREFIX = "hold:";
    private static final Duration HOLD_DURATION = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ShowSeatRepo showSeatRepo;

    public SeatHoldService(RedisTemplate<String, String> redisTemplate, ShowSeatRepo showSeatRepo) {
        this.redisTemplate = redisTemplate;
        this.showSeatRepo = showSeatRepo;
    }
    
    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public boolean areSeatsAvailable(Long showId, List<Long> seatIds) {
        log.info("Checking seat availability for show: {} and seats: {}", showId, seatIds);
        
        try {
            // Only check DB for permanent bookings - Redis holds are checked atomically in holdSeats()
            log.info("Checking DB for permanent bookings for show: {}", showId);
            boolean isBooked = showSeatRepo.existsByShowIdAndSeatIdInAndStatus(showId, seatIds, SeatStatus.BOOKED);
            log.info("DB check for permanent bookings: {}", isBooked);
            
            if (isBooked) {
                throw new SeatAlreadyBookedException("One or more seats are already booked");
            }
            
            return true;
        } catch (Exception e) {
            if (e instanceof SeatAlreadyBookedException) {
                throw e;
            }
            log.error("❌ Error while checking seat availability: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to check seat availability. Please try again later.", e);
        }
    }

    public String holdSeats(Long showId, List<Long> seatIds) {
        String holdId = UUID.randomUUID().toString();
        log.info("Creating hold with ID {} for show {} and seats {}", holdId, showId, seatIds);
        
        // Test Redis connection first with retry logic
        int maxRetries = 3;
        boolean redisAvailable = false;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String testKey = "test:connection:" + holdId + ":" + attempt;
                redisTemplate.opsForValue().set(testKey, "test", Duration.ofSeconds(1));
                String retrieved = redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);
                
                if (!"test".equals(retrieved)) {
                    throw new RuntimeException("Redis test failed - value mismatch");
                }
                redisAvailable = true;
                log.info("✅ Redis connection test successful (attempt {})", attempt);
                break;
            } catch (Exception e) {
                log.warn("⚠️ Redis connection test failed (attempt {}): {}", attempt, e.getMessage());
                if (attempt == maxRetries) {
                    log.error("❌ Redis connection failed after {} attempts", maxRetries);
                    throw new RuntimeException("Redis connection failed after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during Redis retry", ie);
                }
            }
        }
        
        // Try to acquire locks for all seats atomically
        List<String> acquiredKeys = new ArrayList<>();
        try {
            for (Long seatId : seatIds) {
                String key = generateKey(showId, seatId);
                log.info("🔑 Attempting to acquire key: {}", key);
                
                // Modern Redis SET with NX and EX options with retry
                Boolean success = null;
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        success = redisTemplate.opsForValue().setIfAbsent(
                            key,
                            holdId,
                            HOLD_DURATION
                        );
                        break; // Success, exit retry loop
                    } catch (Exception e) {
                        log.warn("⚠️ Redis setIfAbsent failed (attempt {}): {}", attempt, e.getMessage());
                        if (attempt == maxRetries) {
                            throw e;
                        }
                        try {
                            Thread.sleep(500 * attempt); // Short backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted during Redis retry", ie);
                        }
                    }
                }
                
                log.info("📊 Redis setIfAbsent result for key '{}': {} (type: {})", 
                    key, success, success != null ? success.getClass().getSimpleName() : "null");
                
                if (success == null) {
                    log.error("❌ Redis setIfAbsent returned null for key: {}", key);
                    throw new RuntimeException("Redis operation failed - returned null");
                }
                
                if (Boolean.TRUE.equals(success)) {
                    acquiredKeys.add(key);
                    log.info("✅ Successfully acquired seat {} for hold {}", seatId, holdId);
                } else {
                    // If we couldn't acquire all seats, release the ones we did acquire
                    log.warn("⚠️ Failed to acquire seat {} for show {} (already held)", seatId, showId);
                    releaseAcquiredSeats(acquiredKeys, holdId);
                    throw new SeatAlreadyHeldException("Seat " + seatId + " is already held");
                }
            }
            log.info("🎉 Successfully acquired all seats for hold {}", holdId);
            return holdId;
        } catch (Exception e) {
            // If any error occurs, release all acquired seats
            log.error("❌ Error in holdSeats: {}", e.getMessage(), e);
            releaseAcquiredSeats(acquiredKeys, holdId);
            if (e instanceof SeatAlreadyHeldException) {
                throw e;
            }
            throw new RuntimeException("Failed to create seat hold: " + e.getMessage(), e);
        }
    }

    private void releaseAcquiredSeats(List<String> keys, String holdId) {
        for (String key : keys) {
            try {
                // Use Redis transaction to ensure atomic check-and-delete
                redisTemplate.execute(new SessionCallback<Boolean>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Boolean execute(RedisOperations<K, V> operations) {
                        operations.watch((K)key);
                        String storedHoldId = (String) operations.opsForValue().get((K)key);
                        if (storedHoldId != null && storedHoldId.equals(holdId)) {
                            operations.multi();
                            operations.delete((K)key);
                            List<Object> results = operations.exec();
                            return !results.isEmpty();
                        }
                        return false;
                    }
                });
                log.debug("Released hold for key {}", key);
            } catch (Exception e) {
                log.error("Error releasing hold for key {}: {}", key, e.getMessage());
            }
        }
    }

    public boolean validateHold(Long showId, String holdId, List<Long> seatIds) {
        log.info("Validating hold {} for show {} and seats {}", holdId, showId, seatIds);
        
        return redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Boolean execute(RedisOperations<K, V> operations) {
                operations.multi();
                for (Long seatId : seatIds) {
                    String key = generateKey(showId, seatId);
                    operations.opsForValue().get((K)key);
                }
                List<Object> results = operations.exec();
                
                if (results.isEmpty()) return false;
                
                for (Object result : results) {
                    String storedHoldId = (String) result;
                    if (storedHoldId == null || !storedHoldId.equals(holdId)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    public void releaseHold(Long showId, String holdId, List<Long> seatIds) {
        log.info("Releasing hold {} for show {} and seats {}", holdId, showId, seatIds);
        
        for (Long seatId : seatIds) {
            String key = generateKey(showId, seatId);
            try {
                redisTemplate.execute(new SessionCallback<Boolean>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Boolean execute(RedisOperations<K, V> operations) {
                        operations.watch((K)key);
                        String storedHoldId = (String) operations.opsForValue().get((K)key);
                        if (storedHoldId != null && storedHoldId.equals(holdId)) {
                            operations.multi();
                            operations.delete((K)key);
                            List<Object> results = operations.exec();
                            return !results.isEmpty();
                        }
                        return false;
                    }
                });
                log.debug("Released hold for key {}", key);
            } catch (Exception e) {
                log.error("Error releasing hold for key {}: {}", key, e.getMessage());
            }
        }
    }

    private String generateKey(Long showId, Long seatId) {
        return String.format("%s%d:%d", HOLD_KEY_PREFIX, showId, seatId);
    }
} 
