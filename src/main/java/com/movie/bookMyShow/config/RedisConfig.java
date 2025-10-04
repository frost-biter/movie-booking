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
    private boolean redisSslEnabled;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("🚀 Initializing Simplified Redis Configuration...");
        log.info("🔧 Host: {}, Port: {}, SSL Enabled: {}", redisHost, redisPort, redisSslEnabled);

        // 1. Set up the connection details
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        // 2. Configure client-side options like SSL and timeouts
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration.builder();
        
        if (redisSslEnabled) {
            log.info("🔒 SSL is enabled for Redis connection.");
            clientBuilder.useSsl();
        }
        
        // A reasonable timeout is crucial for cloud environments
        clientBuilder.commandTimeout(Duration.ofSeconds(5));

        // 3. Create the factory
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientBuilder.build());

        // 4. THIS IS THE MOST IMPORTANT SETTING FOR STABILITY
        // It ensures the connection pool always gives you a live, working connection, preventing stale connection errors.
        factory.setValidateConnection(true);
        log.info("✅ Connection validation is ENABLED. The pool will test connections before use.");

        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializers for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // Enable transaction support for WATCH/MULTI/EXEC operations
        template.setEnableTransactionSupport(true);

        return template;
    }
}

