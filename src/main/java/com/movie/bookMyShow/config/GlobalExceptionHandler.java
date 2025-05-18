package com.movie.bookMyShow.config;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.exception.CityAlreadyExistsException;
import com.movie.bookMyShow.exception.CityNotFoundException;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyBookedException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleOtherExceptions(Exception ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<ApiResponse> handleSeatAlreadyBooked(SeatAlreadyBookedException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.CONFLICT.value(), ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SeatAlreadyHeldException.class)
    public ResponseEntity<ApiResponse> handleSeatAlreadyHeld(SeatAlreadyHeldException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.CONFLICT.value(), ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleCityNotFound(CityNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CityAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleCityExists(CityAlreadyExistsException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.CONFLICT.value(), ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        return new ResponseEntity<>(new ApiResponse(HttpStatus.UNAUTHORIZED.value(), "Authorization header is required"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        return new ResponseEntity<>(
            new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed: " + errorMessage),
            HttpStatus.BAD_REQUEST
        );
    }
}