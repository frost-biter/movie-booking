package com.movie.bookMyShow.dto;

import com.movie.bookMyShow.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDTO {
    private Long seatId;
    private char row;
    private Long seatNo;
    private SeatCategory category;
    private String seatIdentifier; // Combines row and seat number for easy display
    private boolean isAvailable;
} 