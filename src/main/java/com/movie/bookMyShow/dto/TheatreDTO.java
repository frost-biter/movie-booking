package com.movie.bookMyShow.dto;

import lombok.Data;

import java.util.List;
@Data
public class TheatreDTO {
    private Long theatreId;
    private String theatreName;
    private String address;
    private String movieName;
    private List<ShowDTO> shows;
}
