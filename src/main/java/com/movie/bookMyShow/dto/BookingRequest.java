package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    private Long showId;     // Required: Show being booked
    private List<Long> seatIds;  // List of selected seat numbers
    private String paymentMethod; // Example: UPI, Card, etc.
    private String holdId; // For tracking payment status
    private String phoneNumber;
    private String publicKey = null;
}
