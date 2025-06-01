package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.exception.CityNotFoundException;
import com.movie.bookMyShow.service.CityService;
import com.movie.bookMyShow.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/city")
public class CityController {

    private final JwtUtil jwtUtil;
    @Autowired
    private CityService cityService;

    public CityController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ Endpoint to set the city and return JWT
    @PostMapping("/set")
    public ResponseEntity<?> setCity(@RequestParam String cityName, HttpServletResponse response) {
        Long cityId = cityService.getIdByCity(cityName);
        if(cityId == null){
            throw new CityNotFoundException("City not found: " + cityName);
        }
        String token = jwtUtil.generateToken(cityId);
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        cookie.setSecure(true); // Set to true if using HTTPS
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "City Set successfully. JWT token created."));
    }

    @GetMapping("/get")
    public ResponseEntity<String> getCity(@CookieValue(name = "token", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Integer cityId = jwtUtil.extractCityId(token);
        return ResponseEntity.ok("Extracted cityId: " + cityId);
    }
}