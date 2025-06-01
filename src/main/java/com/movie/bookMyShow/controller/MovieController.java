package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.model.Movie;

import com.movie.bookMyShow.service.MovieService;
import com.movie.bookMyShow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping("/list")
    public ResponseEntity<?> getMoviesByCity(@CookieValue(name = "token", required = false) String token) {
        if (token == null) {
            System.out.println("Token is null or empty");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        Integer cityId = jwtUtil.extractCityId(token); // Extract city from JWT
        System.out.println("City ID from token: " + cityId);
        
        if (cityId == null) {
            System.out.println("No city ID found in token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No city ID found in token");
        }
        
        try {
            List<Movie> movieList = movieService.getMovies(cityId); // Fetch movies for the city
            return ResponseEntity.ok(movieList);
        } catch (Exception e) {
            System.err.println("Error fetching movies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching movies");
        }
    }

}
