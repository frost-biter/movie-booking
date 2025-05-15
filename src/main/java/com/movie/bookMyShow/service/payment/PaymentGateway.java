package com.movie.bookMyShow.service.payment;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.PaymentStatus;
import java.util.concurrent.CompletableFuture;

public interface PaymentGateway {
    CompletableFuture<Boolean> processPayment(BookingRequest request);
    boolean revertPayment(BookingRequest request);
    PaymentStatus checkPaymentStatus(String transactionId);
} 