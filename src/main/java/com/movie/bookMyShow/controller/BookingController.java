package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.BookingResponse;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/seats")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) throws InterruptedException {
        System.out.println("Show ID: " + request.getShowId());
//        System.out.println("Seat ID: " + request.getSeatIds().getFirst());

        BookingResponse response = bookingService.initiateBooking(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<TicketDTO> getBooking(@RequestParam String holdId) throws InterruptedException {

        TicketDTO ticketDTO = bookingService.getBooking(holdId);

        return ResponseEntity.ok(ticketDTO);
    }

}
