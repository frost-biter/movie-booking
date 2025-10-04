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

    @Value("${spring.data.redis.ssl}")
    private boolean redisSsl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("🔧 Configuring Redis connection to Host: {}:{}", redisHost, redisPort);
        log.info("🔧 Redis SSL enabled: {}", redisSsl);
        log.info("🔧 Redis password provided: {}", redisPassword != null && !redisPassword.isEmpty());
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration.builder();
        if (redisSsl) {
            log.info("🔧 Enabling SSL for Redis connection");
            clientBuilder.useSsl();
        }
        
        // Increased timeouts for cloud environments
        clientBuilder.commandTimeout(Duration.ofSeconds(10));
        clientBuilder.shutdownTimeout(Duration.ofSeconds(5));
        
        // Connection pool configuration optimized for Render
        clientBuilder.poolConfig(org.apache.commons.pool2.impl.GenericObjectPoolConfig.builder()
            .maxTotal(10)
            .maxIdle(5)
            .minIdle(1)
            .testOnBorrow(true)
            .testOnReturn(true)
            .testWhileIdle(true)
            .timeBetweenEvictionRunsMillis(30000)
            .build());
        
        LettuceClientConfiguration clientConfig = clientBuilder.build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);

        // --- CRITICAL FIXES FOR CLOUD ENVIRONMENTS ---
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false); // Important for cloud environments
        
        // --- END OF FIXES ---

        return factory;
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