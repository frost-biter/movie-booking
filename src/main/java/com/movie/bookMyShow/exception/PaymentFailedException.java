package com.movie.bookMyShow.exception;

// File: com.movie.bookMyShow.exception.SeatAlreadyBookedException.java
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
