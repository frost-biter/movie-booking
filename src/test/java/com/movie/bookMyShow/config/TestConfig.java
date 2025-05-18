package com.movie.bookMyShow.config;

import com.movie.bookMyShow.service.payment.Crypto.ETHPaymentGateway;
import com.movie.bookMyShow.service.payment.Crypto.EthereumAddressService;
import com.movie.bookMyShow.service.payment.PaymentGateway;
import com.movie.bookMyShow.service.payment.PaymentGatewayFactory;
import com.movie.bookMyShow.service.payment.UPIPaymentGateway;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"book_movie"})
public class TestConfig {
    
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        when(factory.getConnection()).thenReturn(mock(org.springframework.data.redis.connection.RedisConnection.class));
        return factory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    @Primary
    public Web3j web3j() {
        Web3j web3j = mock(Web3j.class);
        try {
            EthGetBalance mockResponse = mock(EthGetBalance.class);
            when(mockResponse.getBalance()).thenReturn(BigInteger.valueOf(100000000000000000L)); // 0.1 ETH in wei
            when(mockResponse.hasError()).thenReturn(false);
            when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return web3j;
    }

    @Bean
    @Primary
    public EthereumAddressService ethereumAddressService() {
        EthereumAddressService service = mock(EthereumAddressService.class);
        when(service.generateNewAddress(anyString())).thenReturn("0x123");
        return service;
    }

    @Bean
    @Primary
    public ETHPaymentGateway ethPaymentGateway() {
        ETHPaymentGateway gateway = mock(ETHPaymentGateway.class);
        when(gateway.generateDepositAddress(anyString())).thenReturn("0x123");
        when(gateway.processPayment(any())).thenReturn(CompletableFuture.completedFuture(true));
        return gateway;
    }

    @Bean
    @Primary
    public UPIPaymentGateway upiPaymentGateway() {
        UPIPaymentGateway gateway = mock(UPIPaymentGateway.class);
        when(gateway.processPayment(any())).thenReturn(CompletableFuture.completedFuture(true));
        return gateway;
    }

    @Bean
    @Primary
    public Map<String, PaymentGateway> paymentGateways() {
        Map<String, PaymentGateway> gateways = new HashMap<>();
        gateways.put("ETH", ethPaymentGateway());
        gateways.put("UPI", upiPaymentGateway());
        return gateways;
    }

    @Bean
    @Primary
    public PaymentGatewayFactory paymentGatewayFactory() {
        PaymentGatewayFactory factory = mock(PaymentGatewayFactory.class);
        when(factory.getPaymentGateway(anyString())).thenAnswer(invocation -> {
            String method = invocation.getArgument(0);
            return paymentGateways().get(method);
        });
        return factory;
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
} 