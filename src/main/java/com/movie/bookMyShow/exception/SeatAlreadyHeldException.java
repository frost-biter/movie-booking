package com.movie.bookMyShow.exception;

public class SeatAlreadyHeldException extends RuntimeException {
    public SeatAlreadyHeldException(String message) {
        super(message);
    }
} 