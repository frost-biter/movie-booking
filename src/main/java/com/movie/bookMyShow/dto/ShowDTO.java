package com.movie.bookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ShowDTO {
    private Long showId;
    private Long movieId;
    private Long theatreId;
    private Long screenId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
