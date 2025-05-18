package com.movie.bookMyShow.dto;

import lombok.Data;

@Data
public class MovieDTO {
    private Long movieId;
    private String movieName;
    private String description;
    private Integer duration;
    private String language;
    private String genre;
} 