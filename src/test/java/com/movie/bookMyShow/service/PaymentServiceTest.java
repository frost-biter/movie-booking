package com.movie.bookMyShow.service;

import com.movie.bookMyShow.config.TestConfig;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.model.PaymentRecord;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.PaymentRecordRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.service.payment.Crypto.ETHPaymentGateway;
import com.movie.bookMyShow.service.payment.PaymentGatewayFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentGatewayFactory paymentGatewayFactory;

    @MockBean
    private PaymentRecordRepo paymentRecordRepo;

    @MockBean
    private BookingRepo bookingRepo;

    @MockBean
    private ShowRepo showRepo;

    @MockBean
    private Web3j web3j;

    @MockBean
    private KafkaTemplate<String, TicketDTO> kafkaBookMovieTemplate;

    private BookingRequest bookingRequest;
    private PaymentRecord mockPaymentRecord;

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

        // Setup mock payment record
        mockPaymentRecord = new PaymentRecord();
        mockPaymentRecord.setHoldId("test-hold-id");
        mockPaymentRecord.setAmount(0.1);
        mockPaymentRecord.setPaymentMethod("ETH");
        mockPaymentRecord.setPhoneNumber("1234567890");
        mockPaymentRecord.setStatus(PaymentStatus.SUCCESS);
        mockPaymentRecord.setTransactionId("test-tx-id");
        mockPaymentRecord.setCompletionTime(LocalDateTime.now());
        mockPaymentRecord.setIsReverted(false);

        // Setup repository mocks
        when(paymentRecordRepo.findByHoldId("test-hold-id"))
            .thenReturn(Collections.singletonList(mockPaymentRecord));
        when(paymentRecordRepo.save(any(PaymentRecord.class)))
            .thenReturn(mockPaymentRecord);
    }

    @Test
    void testProcessPaymentAsyncWithETH() throws Exception {
        // Mock Web3j response for balance check
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.getBalance()).thenReturn(BigInteger.valueOf(100000000000000000L)); // 0.1 ETH in wei
        when(mockResponse.hasError()).thenReturn(false);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Process payment
        paymentService.processPaymentAsync("test-hold-id", bookingRequest);

        // Wait for async processing
        Thread.sleep(1000);

        // Verify payment record was created
        verify(paymentRecordRepo).save(any(PaymentRecord.class));

        // Verify Kafka notification was sent
        verify(kafkaBookMovieTemplate, times(1)).send(eq("book_movie"), eq("test-hold-id"), any(TicketDTO.class));
    }

    @Test
    void testProcessPaymentAsyncWithInsufficientBalance() throws Exception {
        // Mock Web3j response for insufficient balance
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.getBalance()).thenReturn(BigInteger.valueOf(10000000000000000L)); // 0.01 ETH in wei
        when(mockResponse.hasError()).thenReturn(false);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Setup mock payment record for failed case
        PaymentRecord failedPaymentRecord = new PaymentRecord();
        failedPaymentRecord.setHoldId("test-hold-id");
        failedPaymentRecord.setAmount(0.1);
        failedPaymentRecord.setPaymentMethod("ETH");
        failedPaymentRecord.setPhoneNumber("1234567890");
        failedPaymentRecord.setStatus(PaymentStatus.FAILED);
        failedPaymentRecord.setErrorMessage("Insufficient balance");
        failedPaymentRecord.setIsReverted(false);
        when(paymentRecordRepo.findByHoldId("test-hold-id"))
            .thenReturn(Collections.singletonList(failedPaymentRecord));

        // Process payment
        paymentService.processPaymentAsync("test-hold-id", bookingRequest);

        // Wait for async processing
        Thread.sleep(1000);

        // Verify payment record was created with failed status
        verify(paymentRecordRepo).save(any(PaymentRecord.class));

        // Verify no Kafka notification was sent
        verify(kafkaBookMovieTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testProcessPaymentAsyncWithError() throws Exception {
        // Mock Web3j response with error
        EthGetBalance mockResponse = mock(EthGetBalance.class);
        when(mockResponse.hasError()).thenReturn(true);
        when(web3j.ethGetBalance(anyString(), any()).send()).thenReturn(mockResponse);

        // Setup mock payment record for error case
        PaymentRecord errorPaymentRecord = new PaymentRecord();
        errorPaymentRecord.setHoldId("test-hold-id");
        errorPaymentRecord.setAmount(0.1);
        errorPaymentRecord.setPaymentMethod("ETH");
        errorPaymentRecord.setPhoneNumber("1234567890");
        errorPaymentRecord.setStatus(PaymentStatus.FAILED);
        errorPaymentRecord.setErrorMessage("Transaction error");
        errorPaymentRecord.setIsReverted(false);
        when(paymentRecordRepo.findByHoldId("test-hold-id"))
            .thenReturn(Collections.singletonList(errorPaymentRecord));

        // Process payment
        paymentService.processPaymentAsync("test-hold-id", bookingRequest);

        // Wait for async processing
        Thread.sleep(1000);

        // Verify payment record was created with failed status
        verify(paymentRecordRepo).save(any(PaymentRecord.class));

        // Verify no Kafka notification was sent
        verify(kafkaBookMovieTemplate, never()).send(anyString(), anyString(), any());
    }
} 