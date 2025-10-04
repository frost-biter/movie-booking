package com.movie.bookMyShow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class RedisHealthCheck implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSsl;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔍 Starting comprehensive Redis connection test...");
        log.info("🔧 Redis Host: {}", redisHost);
        log.info("🔧 Redis Port: {}", redisPort);
        log.info("🔧 Redis SSL: {}", redisSsl);
        
        int maxRetries = 3;
        boolean connectionSuccessful = false;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("🔄 Redis connection attempt {}/{}", attempt, maxRetries);
                
                // Test 1: Basic SET/GET operation
                String testKey = "health:check:" + System.currentTimeMillis();
                String testValue = "ok";
                
                log.info("📝 Testing SET operation...");
                redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
                
                log.info("📖 Testing GET operation...");
                String retrievedValue = redisTemplate.opsForValue().get(testKey);
                
                if (!testValue.equals(retrievedValue)) {
                    throw new RuntimeException("Value mismatch: expected=" + testValue + ", got=" + retrievedValue);
                }
                
                // Test 2: SETNX operation (critical for seat holds)
                String nxKey = "health:nx:" + System.currentTimeMillis();
                log.info("🔒 Testing SETNX operation...");
                Boolean nxResult = redisTemplate.opsForValue().setIfAbsent(nxKey, "nxValue", Duration.ofSeconds(10));
                
                if (!Boolean.TRUE.equals(nxResult)) {
                    throw new RuntimeException("SETNX operation failed");
                }
                
                // Test 3: DELETE operation
                log.info("🗑️ Testing DELETE operation...");
                redisTemplate.delete(testKey);
                redisTemplate.delete(nxKey);
                
                connectionSuccessful = true;
                log.info("✅ Redis connection test PASSED on attempt {}", attempt);
                log.info("🎉 All Redis operations working correctly!");
                break;
                
            } catch (Exception e) {
                log.error("❌ Redis connection test FAILED on attempt {}: {}", attempt, e.getMessage());
                
                if (attempt == maxRetries) {
                    log.error("💥 All Redis connection attempts failed!");
                    log.error("🔧 Troubleshooting steps:");
                    log.error("   1. Check if Redis server is running");
                    log.error("   2. Verify host and port: {}:{}", redisHost, redisPort);
                    log.error("   3. Check SSL setting: {}", redisSsl);
                    log.error("   4. Verify password is correct");
                    log.error("   5. Check firewall/network connectivity");
                    log.error("   6. For Upstash: Ensure SSL is enabled");
                    throw new RuntimeException("Redis connection failed after " + maxRetries + " attempts", e);
                }
                
                // Wait before retry
                try {
                    Thread.sleep(2000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during Redis retry", ie);
                }
            }
        }
        
        if (connectionSuccessful) {
            log.info("🚀 Redis is ready for seat holding operations!");
        }
    }
}
