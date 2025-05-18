package com.movie.bookMyShow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TheatreDTO {
    private Long theatreId;

    @NotNull(message = "Theatre name is required")
    private String theatreName;

    @NotNull(message = "City is required")
    private String city;

    @NotNull(message = "Address is required")
    private String address;

    private String movieName;
    private List<ShowDTO> shows;
}
