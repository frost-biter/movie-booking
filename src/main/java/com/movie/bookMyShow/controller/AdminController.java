package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private MovieService movieService;

    @PostMapping("/add-movie")
    public ResponseEntity<ApiResponse> addMovie(@RequestBody Movie movie) {
        ApiResponse response = movieService.addMovie(movie);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Autowired
    private ShowService showService;

    @PostMapping("/add-show")
    public ResponseEntity<ApiResponse> addShow(@RequestBody ShowRequest request) {
        ApiResponse response = showService.addShow(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Autowired
    private CityService cityService;

    public ResponseEntity<String> addCity(@RequestBody City city) {
        cityService.addCity(city);
        return ResponseEntity.status(HttpStatus.CREATED).body("City added successfully");
    }

    @Autowired
    private TheatreService theatreService;

    @PostMapping("/add-theatre")
    public ResponseEntity<ApiResponse> addTheatre(@RequestBody Theatre theatre) {
        ApiResponse response = theatreService.addTheatre(theatre);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Autowired
    private ScreenService screenService;

    @PostMapping("/add-screen")
    public ResponseEntity<ApiResponse> addScreen(@RequestBody Screen screen) {

        ApiResponse response = screenService.addScreen(screen);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Autowired
    private SeatService seatService;

    @PostMapping("/add-seat")
    public ResponseEntity<ApiResponse> addSeat(@RequestBody Seat seat) {
        ApiResponse response = seatService.addSeat(seat);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
