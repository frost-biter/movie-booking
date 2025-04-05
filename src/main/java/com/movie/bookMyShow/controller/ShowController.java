package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.service.SeatService;
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
            @RequestAttribute("cityId") Integer cityId) {
        List<TheatreDTO> theatresWithShows = showService.getTheatresWithShows(movieId, cityId);
        return ResponseEntity.ok(theatresWithShows);
    }

    @Autowired
    private SeatService seatService;

    @GetMapping("/show-id-{showId}")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long showId) {
        List<Seat> availableSeats = seatService.getAvailableSeats(showId);
        return ResponseEntity.ok(availableSeats);
    }
}
