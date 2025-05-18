package com.movie.bookMyShow;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    @Autowired
    protected TestDataHelper testDataHelper;

    @BeforeEach
    protected void setUp() {
        testDataHelper.clearAllData();
    }

    protected Show createTestShow() {
        Movie movie = testDataHelper.createMovie("Test Movie", 120);
        Theatre theatre = testDataHelper.createTheatre("Test Theatre");
        Screen screen = testDataHelper.createScreen(theatre, "Screen 1");
        return testDataHelper.createShow(movie, theatre, screen, LocalDateTime.now());
    }

    protected List<Seat> createTestSeats(Screen screen, int count) {
        return testDataHelper.createSeats(screen, count);
    }

    protected BookingRequest createTestBookingRequest(Long showId, List<Long> seatIds) {
        return new BookingRequest(showId, seatIds, "UPI", "1234567890");
    }
} 