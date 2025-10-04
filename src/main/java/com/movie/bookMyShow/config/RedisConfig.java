package com.movie.bookMyShow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSsl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("🚀 Initializing Redis Configuration for Upstash");
        log.info("🔧 Redis Host: {}", redisHost);
        log.info("🔧 Redis Port: {}", redisPort);
        log.info("🔧 Redis SSL Enabled: {}", redisSsl);
        log.info("🔧 Redis Password Provided: {}", redisPassword != null && !redisPassword.isEmpty());
        
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisHost);
            config.setPort(redisPort);
            
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
                log.info("✅ Redis password configured");
            } else {
                log.warn("⚠️ No Redis password provided - this might cause connection issues");
            }

            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration.builder();
            
            // Enable SSL for Upstash (most Upstash instances require SSL)
            if (redisSsl) {
                log.info("🔒 Enabling SSL for Redis connection");
                clientBuilder.useSsl();
            } else {
                log.warn("⚠️ SSL disabled - Upstash typically requires SSL");
            }
            
            // Optimized timeouts for Upstash
            clientBuilder.commandTimeout(Duration.ofSeconds(10));
            clientBuilder.shutdownTimeout(Duration.ofSeconds(5));
            
            // Connection pool settings optimized for Upstash
            clientBuilder.poolConfig(org.apache.commons.pool2.impl.GenericObjectPoolConfig.builder()
                .maxTotal(8)
                .maxIdle(4)
                .minIdle(1)
                .testOnBorrow(true)
                .testOnReturn(true)
                .testWhileIdle(true)
                .timeBetweenEvictionRunsMillis(30000)
                .build());
            
            LettuceClientConfiguration clientConfig = clientBuilder.build();
            LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);

            // Critical settings for cloud Redis
            factory.setValidateConnection(true);
            factory.setShareNativeConnection(false);
            
            log.info("✅ Redis connection factory configured successfully");
            return factory;
            
        } catch (Exception e) {
            log.error("❌ Failed to configure Redis connection factory: {}", e.getMessage(), e);
            throw new RuntimeException("Redis configuration failed", e);
        }
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // Required for SessionCallback (WATCH/MULTI/EXEC) to work correctly.
        template.setEnableTransactionSupport(true);

        return template;
    }
}

