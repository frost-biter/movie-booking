package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private Long showId;
    private String movieName;
    private String theatreName;
    private LocalDateTime showTime;
    private List<SeatDTO> seats;
    private String phoneNumber;
    private LocalDateTime bookingTime;

    @Override
    public String toString() {
        return "TicketDTO{" +
                "showId=" + showId +
                ", movieName='" + movieName + '\'' +
                ", theatreName='" + theatreName + '\'' +
                ", showTime=" + showTime +
                ", seats=" + seats +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bookingTime=" + bookingTime +
                '}';
    }
} 