package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.service.TheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

    @GetMapping
    public ResponseEntity<List<TheatreDTO>> getAllTheatres() {
        return ResponseEntity.ok(theatreService.getAllTheatres());
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreDTO> getTheatreById(@PathVariable Long theatreId) {
        return ResponseEntity.ok(theatreService.getTheatreById(theatreId));
    }

    @GetMapping("/city/{cityName}")
    public ResponseEntity<List<TheatreDTO>> getTheatresByCity(@PathVariable String cityName) {
        return ResponseEntity.ok(theatreService.getTheatresByCity(cityName));
    }
} 