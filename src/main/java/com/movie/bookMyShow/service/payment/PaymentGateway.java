package com.movie.bookMyShow.service.payment;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.PaymentStatus;

public interface PaymentGateway {
    boolean processPayment(BookingRequest request);
    boolean revertPayment(BookingRequest request);
    PaymentStatus checkPaymentStatus(String transactionId);
} 