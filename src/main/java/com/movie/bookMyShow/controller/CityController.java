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

import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/city")
public class CityController {

    private final JwtUtil jwtUtil;
    private final CityService cityService;

    public CityController(JwtUtil jwtUtil, CityService cityService) {
        this.jwtUtil = jwtUtil;
        this.cityService = cityService;
    }

    // ✅ Endpoint to set the city and return JWT
    @GetMapping("/set")
    public ResponseEntity<?> setCity(@RequestParam String cityName) {
        Long cityId = cityService.getIdByCity(cityName);
        if(cityId == null){
            throw new CityNotFoundException("City not found: " + cityName);
        }
        String token = jwtUtil.generateToken(cityId);
        ResponseCookie = ResponseCookie.from("token":token)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(1))
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(Map.of("message","City set Successfully");

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