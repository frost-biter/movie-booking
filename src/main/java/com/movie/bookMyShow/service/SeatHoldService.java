package com.movie.bookMyShow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class SeatHoldService {
    private static final String HOLD_KEY_PREFIX = "hold:";
    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String holdSeats(Long showId, List<Long> seatIds) {
        String holdId = UUID.randomUUID().toString();
        
        // Check if any seat is already held
        for (Long seatId : seatIds) {
            String key = generateKey(showId, seatId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                throw new SeatAlreadyHeldException("Seat " + seatId + " is already held");
            }
        }

        // Create holds
        for (Long seatId : seatIds) {
            String key = generateKey(showId, seatId);
            redisTemplate.opsForValue().set(key, holdId, HOLD_DURATION);
        }

        return holdId;
    }

    public boolean validateHold(Long showId, String holdId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String key = generateKey(showId, seatId);
            String storedHoldId = redisTemplate.opsForValue().get(key);
            if (storedHoldId == null || !storedHoldId.equals(holdId)) {
                return false;
            }
        }
        return true;
    }

    public void releaseHold(Long showId, String holdId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String key = generateKey(showId, seatId);
            String storedHoldId = redisTemplate.opsForValue().get(key);
            if (storedHoldId != null && storedHoldId.equals(holdId)) {
                redisTemplate.delete(key);
            }
        }
    }

    private String generateKey(Long showId, Long seatId) {
        return HOLD_KEY_PREFIX + showId + ":" + seatId;
    }
} 