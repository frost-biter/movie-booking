package com.movie.bookMyShow.service;

import com.movie.bookMyShow.BaseTest;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SeatHoldServiceTest extends BaseTest {

    @Autowired
    private SeatHoldService seatHoldService;

    @Test
    void testSuccessfulSeatHold() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        List<Long> seatIds = List.of(seats.get(0).getSeatId(), seats.get(1).getSeatId());

        // Test
        String holdId = seatHoldService.holdSeats(show.getShowId(), seatIds);

        // Verify
        assertNotNull(holdId);
        assertTrue(seatHoldService.validateHold(show.getShowId(), holdId, seatIds));
    }

    @Test
    void testHoldAlreadyHeldSeats() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        List<Long> seatIds = List.of(seats.get(0).getSeatId());

        // First hold
        String firstHoldId = seatHoldService.holdSeats(show.getShowId(), seatIds);

        // Test - try to hold same seats
        assertThrows(IllegalStateException.class, () -> {
            seatHoldService.holdSeats(show.getShowId(), seatIds);
        });
    }

    @Test
    void testReleaseHold() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        List<Long> seatIds = List.of(seats.get(0).getSeatId());

        // Create hold
        String holdId = seatHoldService.holdSeats(show.getShowId(), seatIds);
        assertTrue(seatHoldService.validateHold(show.getShowId(), holdId, seatIds));

        // Release hold
        seatHoldService.releaseHold(show.getShowId(), holdId, seatIds);

        // Verify seats are available again
        assertTrue(seatHoldService.areSeatsAvailable(show.getShowId(), seatIds));
    }

    @Test
    void testValidateInvalidHold() {
        // Setup
        Show show = createTestShow();
        List<Seat> seats = createTestSeats(show.getScreen(), 5);
        List<Long> seatIds = List.of(seats.get(0).getSeatId());

        // Test with invalid hold ID
        assertFalse(seatHoldService.validateHold(show.getShowId(), UUID.randomUUID().toString(), seatIds));
    }
} 