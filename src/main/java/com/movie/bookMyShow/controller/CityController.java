package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/city")
public class CityController {

    private final JwtUtil jwtUtil;

    public CityController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ Endpoint to set the city and return JWT
    @PostMapping("/set")
    public ResponseEntity<Map<String, String>> setCity(@RequestParam String city) {
        String token = jwtUtil.generateToken(city);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<String> getCity(@RequestHeader("Authorization") String authHeader) {
        // Extract token (remove 'Bearer ' prefix)
        String token = authHeader.replace("Bearer ", "");
        Integer cityId = jwtUtil.extractCityId(token);
        return ResponseEntity.ok("Extracted cityId: " + cityId);
    }
}