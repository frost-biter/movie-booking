package com.movie.bookMyShow.service.payment.Crypto;

import com.movie.bookMyShow.config.TestConfig;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.model.PaymentRecord;
import com.movie.bookMyShow.repo.PaymentRecordRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class ETHPaymentGatewayTest {

    @Autowired
    private ETHPaymentGateway ethPaymentGateway;

    @Autowired
    private Web3j web3j;

    @Autowired
    private PaymentRecordRepo paymentRecordRepo;

    private BookingRequest bookingRequest;
    private PaymentRecord paymentRecord;

    @BeforeEach
    void setUp() {
        // Create test booking request
        bookingRequest = new BookingRequest();
        bookingRequest.setShowId(1L);
        bookingRequest.setSeatIds(Arrays.asList(1L, 2L));
        bookingRequest.setHoldId("test-hold-id");
        bookingRequest.setPaymentMethod("ETH");
        bookingRequest.setPhoneNumber("1234567890");
        bookingRequest.setPrice(0.1); // 0.1 ETH

        // Create test payment record
        paymentRecord = new PaymentRecord();
        paymentRecord.setTransactionId("test-tx-id");
        paymentRecord.setStatus(PaymentStatus.PENDING);
        paymentRecord.setAmount(0.1);
        paymentRecord.setPaymentMethod("ETH");
        paymentRecord.setPhoneNumber("1234567890");
        paymentRecord.setHoldId("test-hold-id");
        paymentRecord.setShowId(1L);
        paymentRecord.setAttemptTime(LocalDateTime.now());
        paymentRecord.setIsReverted(false);
    }

    @Test
    void testGenerateDepositAddress() {
        String address = ethPaymentGateway.generateDepositAddress("test-user");
        assertNotNull(address);
        assertTrue(address.startsWith("0x"));
    }

    @Test
    void testProcessPayment() throws Exception {
        // Mock Web3j response for balance check
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.getBalance()).thenReturn(BigInteger.valueOf(100000000000000000L)); // 0.1 ETH in wei
        when(mockResponse.hasError()).thenReturn(false);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Process payment
        CompletableFuture<Boolean> future = ethPaymentGateway.processPayment(bookingRequest);
        Boolean result = future.get();

        // Verify payment was processed successfully
        assertTrue(result);
        verify(web3j, atLeastOnce()).ethGetBalance(anyString(), any());
    }

    @Test
    void testProcessPaymentWithInsufficientBalance() throws Exception {
        // Mock Web3j response for insufficient balance
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.getBalance()).thenReturn(BigInteger.valueOf(10000000000000000L)); // 0.01 ETH in wei
        when(mockResponse.hasError()).thenReturn(false);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Process payment
        CompletableFuture<Boolean> future = ethPaymentGateway.processPayment(bookingRequest);
        Boolean result = future.get();

        // Verify payment failed due to insufficient balance
        assertFalse(result);
        verify(web3j, atLeastOnce()).ethGetBalance(anyString(), any());
    }

    @Test
    void testProcessPaymentWithError() throws Exception {
        // Mock Web3j response with error
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.hasError()).thenReturn(true);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Process payment
        CompletableFuture<Boolean> future = ethPaymentGateway.processPayment(bookingRequest);
        Boolean result = future.get();

        // Verify payment failed due to error
        assertFalse(result);
        verify(web3j, atLeastOnce()).ethGetBalance(anyString(), any());
    }
} 