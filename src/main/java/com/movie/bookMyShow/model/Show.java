package com.movie.bookMyShow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showId;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false) // Foreign key for Movie
    private Movie movie; // Use object reference instead of just movieId

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false) // Foreign key for Screen
    private Screen screen; // Use object reference instead of just screenId

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "show_booked_seats",
            joinColumns = @JoinColumn(name = "show_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @JsonIgnore
    private List<Seat> bookedSeats ; // Stores actual Seat objects

    @ManyToOne
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonIgnore
    private Theatre theatre;

//    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
//    private List<Ticket> tickets;
    @PrePersist
    @PreUpdate
    private void prePersistAndValidate() {
        // 1. Calculate endTime
        if (startTime != null && movie != null) {
            this.endTime = startTime.plusMinutes(movie.getDuration());
        }

        // 2. Validate theatre-screen consistency
        if (screen != null && theatre != null &&
                !screen.getTheatre().getTheatreId().equals(theatre.getTheatreId())) {
            throw new IllegalStateException(
                    "Screen " + screen.getScreenId() +
                            " does not belong to Theatre " + theatre.getTheatreId()
            );
        }
    }

    public void addBookedSeat(Seat seat) {
        if (bookedSeats == null) {
            bookedSeats = new java.util.ArrayList<>();
        }
        bookedSeats.add(seat);
    }
}
