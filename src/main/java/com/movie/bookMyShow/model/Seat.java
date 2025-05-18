package com.movie.bookMyShow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.bookMyShow.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Column(name = "row_number")
    private char row;

    private Long seatNo;

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false) // 🔥 Ensure this exists
    private Screen screen;  // �� This must be here

    @Enumerated(EnumType.STRING)
    private SeatCategory category;
}
