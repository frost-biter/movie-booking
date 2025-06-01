package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.SeatDTO;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.service.ShowSeatService;
import com.movie.bookMyShow.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shows")
public class ShowController {
    @Autowired
    private ShowService showService;

    @GetMapping("/movie-id-{movieId}")
    public ResponseEntity<List<TheatreDTO>> getTheatresWithShows(
            @PathVariable int movieId,
            @RequestAttribute(value = "cityId", required = false) Integer cityId) {
        if (cityId == null) {
            throw new IllegalArgumentException("City ID is required");
        }
        List<TheatreDTO> theatresWithShows = showService.getTheatresWithShows(movieId, cityId);
        return ResponseEntity.ok(theatresWithShows);
    }

    @Autowired
    private ShowSeatService showSeatService;

    @GetMapping("/show-id-{showId}")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(@PathVariable Long showId) {
        List<SeatDTO> availableSeats = showSeatService.getAvailableSeats(showId);
        return ResponseEntity.ok(availableSeats);
    }
}
