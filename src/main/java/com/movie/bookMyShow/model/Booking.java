package com.movie.bookMyShow.model;

import com.movie.bookMyShow.enums.BookingStatus;
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
    private String holdId;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false) // Foreign key for Screen
    private Show show; // Use object reference instead of just screenId

    @ManyToMany
    @JoinTable(
            name = "booking_seats",
            joinColumns = @JoinColumn(name = "hold_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats; // Stores actual Seat objects

    private double price;

    private String phoneNumber; // User can enter their phone number for tracking

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;
}
