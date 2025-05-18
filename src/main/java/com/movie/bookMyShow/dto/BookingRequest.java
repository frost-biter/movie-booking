package com.movie.bookMyShow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;     // Required: Show being booked

    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;  // List of selected seat numbers

    private String holdId; // For tracking payment status

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // Example: UPI, Card, ETH, etc.

    @NotNull(message = "Phone number is required")
    private String phoneNumber;

    private String publicKey; // For crypto payments

    private double price; // Required amount for payment verification

    public BookingRequest(Long showId, List<Long> seatIds, String paymentMethod, String phoneNumber) {
        this.showId = showId;
        this.seatIds = seatIds;
        this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber;
    }
}
