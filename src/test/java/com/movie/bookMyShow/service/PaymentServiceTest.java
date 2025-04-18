package com.movie.bookMyShow.service;

import com.movie.bookMyShow.BaseTest;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest extends BaseTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SeatHoldService seatHoldService;

    @Test
    void testSuccessfulPaymentAndBooking() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        BookingRequest request = createTestBookingRequest(show.getShowId(), 
            List.of(seats.get(0).getSeatId(), seats.get(1).getSeatId()));
        
        String holdId = UUID.randomUUID().toString();
        seatHoldService.holdSeats(show.getShowId(), request.getSeatIds());

        // Test
        TicketDTO ticket = paymentService.createBookingAndGetTicket(request, holdId);
        
        // Verify
        assertNotNull(ticket);
        assertEquals(show.getShowId(), ticket.getShowId());
        assertEquals(2, ticket.getSeats().size());
        assertEquals(request.getPhoneNumber(), ticket.getPhoneNumber());
    }

    @Test
    void testPaymentWithInvalidHold() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        BookingRequest request = createTestBookingRequest(show.getShowId(), 
            List.of(seats.get(0).getSeatId()));
        
        String invalidHoldId = UUID.randomUUID().toString();

        // Test
        assertThrows(IllegalStateException.class, () -> {
            paymentService.createBookingAndGetTicket(request, invalidHoldId);
        });
    }

    @Test
    void testPaymentWithInvalidSeat() {
        // Setup
        Show show = createTestShow();
        BookingRequest request = createTestBookingRequest(show.getShowId(), List.of(999L));
        
        String holdId = UUID.randomUUID().toString();
        seatHoldService.holdSeats(show.getShowId(), request.getSeatIds());

        // Test
        assertThrows(IllegalStateException.class, () -> {
            paymentService.createBookingAndGetTicket(request, holdId);
        });
    }
} 