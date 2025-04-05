package com.movie.bookMyShow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show; // The show being booked

    private int numberOfSeats;

    private String phoneNumber; // User can enter their phone number for tracking

    private LocalDateTime bookingTime;

    @OneToMany
    private List<Seat> seats; // List of seats booked
}
