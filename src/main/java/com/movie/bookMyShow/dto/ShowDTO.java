package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ShowDTO {
    private int showId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
