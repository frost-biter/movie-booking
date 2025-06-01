package com.movie.bookMyShow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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
    @JsonIgnore
    private Screen screen; // Use object reference instead of just screenId

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    private List<ShowSeat> showSeats = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonIgnore
    private Theatre theatre;

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
}
