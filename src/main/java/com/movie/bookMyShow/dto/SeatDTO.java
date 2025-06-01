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

    public static SeatDTO fromSeat(com.movie.bookMyShow.model.Seat seat) {
        return new SeatDTO(
            seat.getSeatId(),
            seat.getRow(),
            seat.getSeatNo(),
            seat.getCategory(),
            seat.getRow() + String.valueOf(seat.getSeatNo())
        );
    }
} 