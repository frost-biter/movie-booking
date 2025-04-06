package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.exception.CityNotFoundException;
import com.movie.bookMyShow.service.CityService;
import com.movie.bookMyShow.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/city")
public class CityController {

    private final JwtUtil jwtUtil;
    @Autowired
    private UserController userController;
    @Autowired
    private CityService cityService;

    public CityController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ Endpoint to set the city and return JWT
    @PostMapping("/set")
    public ResponseEntity<Map<String, String>> setCity(@RequestParam String cityName) {
        Long cityId = cityService.getIdByCity(cityName);
        if(cityId == null){
            throw new CityNotFoundException("City not found: " + cityName);
        }
        String token = jwtUtil.generateToken(cityId);
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

    @GetMapping("/slow")
//    @Async
    public void SlowDown1() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        System.out.print("At : " + 0 + " started 1"+Thread.currentThread().getName()+"\n");
        userController.SlowDown2(startTime);
        userController.SlowDown3(startTime);
        long endTime = System.currentTimeMillis();

        System.out.print("At : " + (endTime - startTime) + " ended 1"+"\n");
    }
}