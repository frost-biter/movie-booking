package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.model.Movie;
import com.movie.bookMyShow.service.BookingService;
import com.movie.bookMyShow.service.MovieService;
import com.movie.bookMyShow.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService; // Dependency Injection
    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping("/list")
    public ResponseEntity<List<Movie>> getMoviesByCity(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.replace("Bearer ", "");

        Integer cityId = jwtUtil.extractCityId(token); // Extract city from JWT
        System.out.print("City ID: " + cityId);
        List<Movie> movieList = movieService.getMovies(cityId); // Fetch movies for the city
        return ResponseEntity.ok(movieList);
    }

}
