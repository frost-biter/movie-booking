package com.movie.bookMyShow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl:false}")
    private boolean redisSsl;

    @Value("${spring.data.redis.timeout:10000}")
    private int redisTimeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("🔧 Configuring Redis connection...");
        log.info("📍 Redis Host: {}", redisHost);
        log.info("🔌 Redis Port: {}", redisPort);
        log.info("🔐 Redis Password: {}", redisPassword != null && !redisPassword.isEmpty() ? "***SET***" : "NOT SET");
        log.info("🔒 Redis SSL: {}", redisSsl);
        log.info("⏱️ Redis Timeout: {}ms", redisTimeout);
        
        // Configure Redis connection
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        // Configure Lettuce client (SSL optional)
        LettuceClientConfiguration.Builder clientBuilder = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisTimeout))
                .shutdownTimeout(Duration.ofMillis(100));
        if (redisSsl) {
            clientBuilder.useSsl();
        }
        LettuceClientConfiguration clientConfig = clientBuilder.build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false);
        
        // Initialize and ping
        try {
            factory.afterPropertiesSet();
            String ping = factory.getConnection().ping();
            log.info("✅ Redis ping response: {}", ping);
        } catch (Exception ex) {
            log.error("❌ Redis ping failed: {}", ex.getMessage(), ex);
        }
        
        log.info("✅ Redis connection factory created successfully");
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
