package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.model.Ticket;
import com.movie.bookMyShow.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/seats")
    public ResponseEntity<Ticket> createBooking(@RequestBody BookingRequest request) throws InterruptedException {
        System.out.println("Show ID: " + request.getShowId());
        System.out.println("Seat ID: " + request.getSeatIds().getFirst());

        Ticket ticket = bookingService.bookSeats(request);

        return ResponseEntity.ok(ticket);
    }

}
