package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private String message;
    private String holdId;
    private String paymentAddress = null;
    private String paymentMethod = null;
    private Double price = null;
    
} 