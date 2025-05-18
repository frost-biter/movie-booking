package com.movie.bookMyShow.service.payment;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UPIPaymentGateway implements PaymentGateway {
    
    @Override
    @Async
    public CompletableFuture<Boolean> processPayment(BookingRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing UPI payment for request: {}", request);
            try {
                // Simulate UPI payment processing
                Thread.sleep(5000);
                boolean success = Math.random() > 0.1; // 90% success rate
                log.info("UPI payment result: {}", success);
                return success;
            } catch (Exception e) {
                log.error("Error processing UPI payment: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public boolean revertPayment(BookingRequest request) {
        log.info("Reverting UPI payment for request: {}", request);
        try {
            // Simulate UPI payment reversal
            Thread.sleep(3000);
            boolean success = Math.random() > 0.05; // 95% success rate
            log.info("UPI payment reversal result: {}", success);
            return success;
        } catch (Exception e) {
            log.error("Error reverting UPI payment: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public PaymentStatus checkPaymentStatus(String transactionId, double requiredAmount) {
        log.info("Checking UPI payment status for transaction: {} with amount: {}", transactionId, requiredAmount);
        try {
            // Simulate UPI payment status check
            Thread.sleep(1000);
            boolean success = Math.random() > 0.1; // 90% success rate
            log.info("UPI payment status check result: {}", success);
            return success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        } catch (Exception e) {
            log.error("Error checking UPI payment status: {}", e.getMessage());
            return PaymentStatus.FAILED;
        }
    }
} 