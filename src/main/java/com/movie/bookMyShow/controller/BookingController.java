package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.model.Ticket;
import com.movie.bookMyShow.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/seats")
    public ResponseEntity<Ticket> createBooking(@RequestBody BookingRequest request) {
        System.out.println("Show ID: " + request.getShowId());
        System.out.println("Seat ID: " + request.getSeatIds().getFirst());

        Ticket ticket = bookingService.bookSeats(request);

        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}
