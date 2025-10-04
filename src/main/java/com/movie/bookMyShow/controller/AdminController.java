package com.movie.bookMyShow.controller;

import lombok.extern.slf4j.Slf4j;
import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.CredentialsRequest;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.service.*;
import com.movie.bookMyShow.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private MovieService movieService;

    @Autowired
    private SeatHoldService seatHoldService;

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
    public ResponseEntity<?> login(@RequestBody CredentialsRequest request , HttpServletResponse response) {
        String username = request.getUsername();
        // 1. Validate credentials
        Admin admin = adminService.findByUsername(username);
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) { // You should hash and compare
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
        String role = admin.getRole(); // fetch role from DB
        String token = jwtUtil.generateAdminToken(username, role);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // only over HTTPS
        
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        // For development with localhost
        cookie.setAttribute("SameSite", "None");

        // If you deploy to production with a domain, uncomment and set your domain
        // cookie.setDomain("yourdomain.com");

        response.addCookie(cookie);
        return ResponseEntity.ok("Login successful");
    }
    
    @GetMapping("/redis-health")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        try {
            log.info("🔍 Admin endpoint: Testing Redis connection...");
            
            boolean isHealthy = seatHoldService.testRedisConnection();
            
            return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "redis_connection", isHealthy,
                "timestamp", System.currentTimeMillis(),
                "message", isHealthy ? "Redis is working properly" : "Redis connection failed"
            ));
            
        } catch (Exception e) {
            log.error("❌ Redis health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "DOWN",
                "redis_connection", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis(),
                "message", "Redis connection failed: " + e.getMessage()
            ));
        }
    }
}
