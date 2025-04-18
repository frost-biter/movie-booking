package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.CredentialsRequest;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.service.*;
import com.movie.bookMyShow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @PostMapping("/add-city")
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
    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CredentialsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.register(request));
    }

    private final JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    AdminController (JwtUtil jwtUtil) {this.jwtUtil = jwtUtil;}
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody CredentialsRequest request) {
        String username = request.getUsername();
        // 1. Validate credentials
        Admin admin = adminService.findByUsername(username);
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) { // You should hash and compare
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        String role = admin.getRole(); // fetch role from DB
        String token = jwtUtil.generateAdminToken(username, role);

        return ResponseEntity.ok(Map.of("token", token));
    }
}
