package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowRequest {
    private int movieId;
    private int theatreId;
    private int screenId;
    private LocalDateTime startTime;
}
