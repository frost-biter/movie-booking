package com.movie.bookMyShow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisHealthCheck implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("🔍 Testing Redis connection...");
            
            // Test basic Redis operations
            String testKey = "health:check";
            String testValue = "ok";
            
            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            
            if (testValue.equals(retrievedValue)) {
                log.info("Original value: {}", testValue);
                log.info("Retrieved value: {}", retrievedValue);
                log.info("✅ Redis connection is working properly!");
                // Clean up test key
                redisTemplate.delete(testKey);
            } else {
                log.error("❌ Redis connection test failed - value mismatch");
            }
            
        } catch (Exception e) {
            log.error("❌ Redis connection failed: {}", e.getMessage(), e);
            log.error("🔧 Please check your Redis configuration:");
            log.error("   - SPRING_DATA_REDIS_HOST");
            log.error("   - SPRING_DATA_REDIS_PORT");
            log.error("   - SPRING_DATA_REDIS_PASSWORD");
            log.error("   - SPRING_DATA_REDIS_SSL");
        }
    }
}
