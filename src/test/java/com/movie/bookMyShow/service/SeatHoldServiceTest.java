package com.movie.bookMyShow.service;

import com.movie.bookMyShow.config.TestConfig;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class SeatHoldServiceTest {

    @MockBean
    private SeatHoldService seatHoldService;

    @MockBean
    private ShowRepo showRepo;

    @MockBean
    private ShowSeatRepo showSeatRepo;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    private Show testShow;
    private List<Long> testSeatIds;

    @BeforeEach
    void setUp() {
        // Setup Redis mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn("test-hold-id");
        doNothing().when(redisTemplate).delete(anyString());

        // Create test show
        testShow = new Show();
        testShow.setShowId(1L);
        when(showRepo.findById(1L)).thenReturn(java.util.Optional.of(testShow));
        when(showRepo.findById(999L)).thenReturn(java.util.Optional.empty());

        // Create test seats
        testSeatIds = Arrays.asList(1L, 2L);

        // Setup SeatHoldService mocks
        when(seatHoldService.holdSeats(anyLong(), anyList())).thenReturn("test-hold-id");
        when(seatHoldService.validateHold(anyLong(), anyString(), anyList())).thenReturn(true);
        when(seatHoldService.areSeatsAvailable(anyLong(), anyList())).thenReturn(true);
    }

    @Test
    void testHoldSeats() {
        // Hold seats
        String holdId = seatHoldService.holdSeats(testShow.getShowId(), testSeatIds);
        
        // Verify hold was created
        assertNotNull(holdId);
        assertTrue(seatHoldService.validateHold(testShow.getShowId(), holdId, testSeatIds));
        
        // Verify Redis operations
        verify(valueOperations).setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testHoldSeatsWithInvalidShow() {
        // Try to hold seats for non-existent show
        String holdId = seatHoldService.holdSeats(999L, testSeatIds);
        
        // Verify hold failed
        assertNull(holdId);
        
        // Verify Redis operations were not called
        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testValidateHold() {
        // Create a hold
        String holdId = seatHoldService.holdSeats(testShow.getShowId(), testSeatIds);
        assertNotNull(holdId);

        // Validate the hold
        assertTrue(seatHoldService.validateHold(testShow.getShowId(), holdId, testSeatIds));
        
        // Verify Redis operations
        verify(valueOperations).get(anyString());
    }

    @Test
    void testValidateHoldWithInvalidHoldId() {
        // Mock Redis to return null for invalid hold ID
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // Try to validate non-existent hold
        assertFalse(seatHoldService.validateHold(testShow.getShowId(), "invalid-hold-id", testSeatIds));
        
        // Verify Redis operations
        verify(valueOperations).get(anyString());
    }

    @Test
    void testValidateHoldWithInvalidSeats() {
        // Create a hold
        String holdId = seatHoldService.holdSeats(testShow.getShowId(), testSeatIds);
        assertNotNull(holdId);

        // Try to validate with different seats
        List<Long> differentSeats = Arrays.asList(3L, 4L);
        assertFalse(seatHoldService.validateHold(testShow.getShowId(), holdId, differentSeats));
    }

    @Test
    void testReleaseHold() {
        // Create a hold
        String holdId = seatHoldService.holdSeats(testShow.getShowId(), testSeatIds);
        assertNotNull(holdId);

        // Release the hold
        seatHoldService.releaseHold(testShow.getShowId(), holdId, testSeatIds);

        // Verify hold is no longer valid
        assertFalse(seatHoldService.validateHold(testShow.getShowId(), holdId, testSeatIds));
        
        // Verify Redis operations
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void testAreSeatsAvailable() {
        // Verify seats are available initially
        assertTrue(seatHoldService.areSeatsAvailable(testShow.getShowId(), testSeatIds));

        // Hold the seats
        String holdId = seatHoldService.holdSeats(testShow.getShowId(), testSeatIds);
        assertNotNull(holdId);

        // Verify seats are no longer available
        assertFalse(seatHoldService.areSeatsAvailable(testShow.getShowId(), testSeatIds));

        // Release the hold
        seatHoldService.releaseHold(testShow.getShowId(), holdId, testSeatIds);

        // Verify seats are available again
        assertTrue(seatHoldService.areSeatsAvailable(testShow.getShowId(), testSeatIds));
    }
} 