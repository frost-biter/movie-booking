package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/seats")
    public ResponseEntity<ApiResponse> createBooking(@RequestBody BookingRequest request) throws InterruptedException {
        System.out.println("Show ID: " + request.getShowId());
        System.out.println("Seat ID: " + request.getSeatIds().getFirst());

        ApiResponse response = bookingService.initiateBooking(request);

        return ResponseEntity.ok(response);
    }

}
