package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.movie.bookMyShow.model.Seat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {

    private Long showId;
    private String movieName;
    private String theatreName;
    private LocalDateTime showTime;
    private List<Seat> seats;
    private String phoneNumber;
    private LocalDateTime bookingTime;

    @Override
    public String toString() {
        return "TicketDTO{" +
                "showId=" + showId +
                ", movieName='" + movieName + '\'' +
                ", theatreName='" + theatreName + '\'' +
                ", showTime=" + showTime +
                ", seats=" + seats.stream()
                    .map(seat -> "Seat{row=" + seat.getRow() + ", number=" + seat.getSeatNo() + "}")
                    .collect(Collectors.toList()) +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bookingTime=" + bookingTime +
                '}';
    }
} 