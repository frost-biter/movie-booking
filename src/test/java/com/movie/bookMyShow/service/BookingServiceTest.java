package com.movie.bookMyShow.service;

import com.movie.bookMyShow.BaseTest;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest extends BaseTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatHoldService seatHoldService;

    @Test
    void testSuccessfulBookingFlow() throws InterruptedException {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        BookingRequest request = createTestBookingRequest(show.getShowId(), 
            List.of(seats.get(0).getSeatId(), seats.get(1).getSeatId()));

        // Test
        var response = bookingService.initiateBooking(request);
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Payment process initiated"));
        assertTrue(response.getMessage().contains("Hold ID:"));

        // Extract holdId from response
        String holdId = response.getMessage().split("Hold ID: ")[1].trim();

        // Wait for payment processing (since it's async)
        TimeUnit.SECONDS.sleep(15);

        // Verify booking was created
        var ticket = bookingService.getBooking(holdId);
        assertNotNull(ticket);
        assertEquals(show.getShowId(), ticket.getShowId());
        assertEquals(2, ticket.getSeats().size());
    }

    @Test
    void testBookingWithUnavailableSeats() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        
        // Book seats first
        BookingRequest firstRequest = createTestBookingRequest(show.getShowId(), 
            List.of(seats.get(0).getSeatId()));
        bookingService.initiateBooking(firstRequest);

        // Try to book same seats again
        BookingRequest secondRequest = createTestBookingRequest(show.getShowId(), 
            List.of(seats.get(0).getSeatId()));

        // Test
        assertThrows(SeatAlreadyHeldException.class, () -> {
            bookingService.initiateBooking(secondRequest);
        });
    }

    @Test
    void testBookingWithInvalidShowId() {
        // Setup
        BookingRequest request = createTestBookingRequest(999L, List.of(1L));

        // Test
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.initiateBooking(request);
        });
    }

    @Test
    void testGetBookingWithInvalidHoldId() {
        // Test
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.getBooking("invalid-hold-id");
        });
    }
} 