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
import java.util.stream.Collectors;

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

    /**
     * Atomically holds seats for a given show.
     * This method first checks for permanent bookings in the database, then attempts to acquire temporary holds in Redis.
     * It replaces the need for a separate areSeatsAvailable() check, thus preventing race conditions.
     *
     * @param showId  The ID of the show.
     * @param seatIds The list of seat IDs to hold.
     * @return A unique hold ID if successful.
     * @throws SeatAlreadyBookedException if one or more seats are already permanently booked.
     * @throws SeatAlreadyHeldException   if one or more seats are already held by another user.
     * @throws RuntimeException           if a Redis connection issue occurs.
     */
    public String holdSeats(Long showId, List<Long> seatIds) {
        String holdId = UUID.randomUUID().toString();
        log.info("Attempting to create hold with ID {} for show {} and seats {}", holdId, showId, seatIds);

        // 1. Test Redis connection first
        if (!testRedisConnection()) {
            log.error("Redis connection test failed - cannot proceed with seat holds");
            throw new RuntimeException("Redis service is temporarily unavailable. Please try again later.");
        }

        // 2. Check DB for permanent bookings first. This is a fast and critical check.
        if (showSeatRepo.existsByShowIdAndSeatIdInAndStatus(showId, seatIds, SeatStatus.BOOKED)) {
            log.warn("Hold failed: One or more seats for show {} are already permanently booked.", showId);
            throw new SeatAlreadyBookedException("One or more of the selected seats are already booked.");
        }

        // 3. Atomically acquire temporary holds in Redis with retry logic
        List<String> acquiredKeys = new ArrayList<>();
        int maxRetries = 3;
        
        try {
            for (Long seatId : seatIds) {
                String key = generateKey(showId, seatId);
                Boolean success = null;
                
                // Retry logic for Redis operations
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        success = redisTemplate.opsForValue().setIfAbsent(key, holdId, HOLD_DURATION);
                        log.info("Attempted Redis SETNX for key '{}' (attempt {}). Success: {}", key, attempt, success);
                        
                        if (success != null) {
                            break; // Success, exit retry loop
                        }
                        
                        if (attempt < maxRetries) {
                            log.warn("Redis SETNX returned null for key '{}' (attempt {}), retrying...", key, attempt);
                            try {
                                Thread.sleep(1000 * attempt); // Exponential backoff
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Interrupted during Redis retry", ie);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Redis SETNX failed for key '{}' (attempt {}): {}", key, attempt, e.getMessage());
                        if (attempt == maxRetries) {
                            throw e;
                        }
                        try {
                            Thread.sleep(1000 * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted during Redis retry", ie);
                        }
                    }
                }

                if (success == null) {
                    log.error("Redis command returned null for key '{}' after {} attempts. This indicates a connection problem.", key, maxRetries);
                    throw new RuntimeException("Could not hold seats due to a Redis connection issue. Please try again.");
                }

                if (success) {
                    acquiredKeys.add(key);
                    log.info("✅ Successfully acquired seat {} for hold {}", seatId, holdId);
                } else {
                    // This is the race condition outcome: another user got this seat first.
                    log.warn("Failed to acquire seat hold for key '{}'. It was already held.", key);
                    throw new SeatAlreadyHeldException("Seat " + seatId + " was just held by another user. Please select another seat.");
                }
            }
            log.info("Successfully acquired all {} seats for hold ID {}", acquiredKeys.size(), holdId);
            return holdId;
        } catch (Exception e) {
            // If any error occurs (e.g., SeatAlreadyHeldException or connection error), release any holds we did manage to get.
            releaseAcquiredSeats(acquiredKeys, holdId);
            // Re-throw the original exception to be handled by the controller.
            throw e;
        }
    }

    private void releaseAcquiredSeats(List<String> keys, String holdId) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        log.info("Releasing {} acquired keys for failed hold attempt {}", keys.size(), holdId);
        // A simple multi-delete is sufficient for cleanup after a failed `holdSeats` attempt.
        redisTemplate.delete(keys);
    }

    public boolean validateHold(Long showId, String holdId, List<Long> seatIds) {
        log.info("Validating hold {} for show {} and seats {}", holdId, showId, seatIds);
        List<String> keys = seatIds.stream()
                .map(seatId -> generateKey(showId, seatId))
                .collect(Collectors.toList());
        
        // MGET is more efficient than multiple GETs in a MULTI/EXEC block for simple validation.
        List<String> storedHoldIds = redisTemplate.opsForValue().multiGet(keys);

        if (storedHoldIds == null || storedHoldIds.size() != seatIds.size()) {
             log.warn("Hold validation failed for {}: MGET returned unexpected result.", holdId);
             return false;
        }

        // Check if all keys exist and belong to the correct holdId.
        for (String storedId : storedHoldIds) {
            if (storedId == null || !storedId.equals(holdId)) {
                log.warn("Hold validation failed for {}: A seat was not held or held by someone else.", holdId);
                return false;
            }
        }
        log.info("Hold {} successfully validated.", holdId);
        return true;
    }

    public void releaseHold(Long showId, String holdId, List<Long> seatIds) {
        log.info("Releasing hold {} for show {} and seats {}", holdId, showId, seatIds);
        List<String> keys = seatIds.stream()
                .map(seatId -> generateKey(showId, seatId))
                .collect(Collectors.toList());
        
        // We can add a WATCH/MULTI/EXEC here for a "safe" release if needed,
        // but for a confirmed booking, a simple delete is usually sufficient.
        redisTemplate.delete(keys);
        log.info("Successfully released hold {}", holdId);
    }

    private String generateKey(Long showId, Long seatId) {
        return String.format("%s%d:%d", HOLD_KEY_PREFIX, showId, seatId);
    }
    
    /**
     * Test Redis connection with basic operations
     * @return true if Redis is working, false otherwise
     */
    public boolean testRedisConnection() {
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "test";
            
            // Test basic SET operation
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(5));
            
            // Test GET operation
            String retrieved = redisTemplate.opsForValue().get(testKey);
            
            // Test SETNX operation (critical for seat holds)
            String nxKey = "test:nx:" + System.currentTimeMillis();
            Boolean nxResult = redisTemplate.opsForValue().setIfAbsent(nxKey, "nxValue", Duration.ofSeconds(5));
            
            // Clean up
            redisTemplate.delete(testKey);
            redisTemplate.delete(nxKey);
            
            boolean isWorking = testValue.equals(retrieved) && Boolean.TRUE.equals(nxResult);
            
            if (isWorking) {
                log.info("✅ Redis connection test passed");
            } else {
                log.error("❌ Redis connection test failed - SET/GET: {}, SETNX: {}", 
                    testValue.equals(retrieved), Boolean.TRUE.equals(nxResult));
            }
            
            return isWorking;
            
        } catch (Exception e) {
            log.error("❌ Redis connection test failed with exception: {}", e.getMessage(), e);
            return false;
        }
    }
}

