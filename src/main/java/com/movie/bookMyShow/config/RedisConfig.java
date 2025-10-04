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

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean redisSsl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("✅✅✅ Initializing Simplified RedisConfig. The code is new! ✅✅✅");
        log.info("🔧 Configuring Redis connection to Host: {}", redisHost);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration.builder();
        if (redisSsl) {
            clientBuilder.useSsl();
        }
        // Set a reasonable command timeout
        clientBuilder.commandTimeout(Duration.ofSeconds(5));
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientBuilder.build());

        // --- THIS IS THE CRITICAL FIX FOR STALE CONNECTIONS ---
        // This tells the factory to test connections before they are used,
        // preventing your code from ever getting a "stale" or dead connection.
        factory.setValidateConnection(true);
        log.info("🔧🔧🔧 Redis connection validation has been set to TRUE. 🔧🔧🔧");

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

