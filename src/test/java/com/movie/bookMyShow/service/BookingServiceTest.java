package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.BookingResponse;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGateway;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGatewayFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookingServiceTest {

    @Mock
    private ShowRepo showRepo;

    @Mock
    private SeatHoldService seatHoldService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private BookingRepo bookingRepo;

    @Mock
    private CryptoGatewayFactory cryptoGatewayFactory;

    @Mock
    private CryptoGateway cryptoGateway;

    @InjectMocks
    private BookingService bookingService;

    private Show testShow;
    private BookingRequest testRequest;
    private List<Long> testSeatIds;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test show
        testShow = new Show();
        testShow.setShowId(1L);
        Movie movie = new Movie();
        movie.setMovieName("Test Movie");
        testShow.setMovie(movie);
        Theatre theatre = new Theatre();
        theatre.setTheatreName("Test Theatre");
        testShow.setTheatre(theatre);
        testShow.setStartTime(LocalDateTime.now().plusHours(2));

        // Setup test request
        testSeatIds = Arrays.asList(1L, 2L);
        testRequest = new BookingRequest();
        testRequest.setShowId(1L);
        testRequest.setSeatIds(testSeatIds);
        testRequest.setPaymentMethod("ETH");
        testRequest.setPhoneNumber("1234567890");
    }

    @Test
    void testSuccessfulCryptoBooking() {
        // Arrange
        when(showRepo.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatHoldService.areSeatsAvailable(any(), any())).thenReturn(true);
        when(seatHoldService.holdSeats(any(), any())).thenReturn("test-hold-id");
        when(cryptoGatewayFactory.getCryptoGateway("ETH")).thenReturn(cryptoGateway);
        when(cryptoGateway.generateDepositAddress(any())).thenReturn("0x123");

        // Act
        BookingResponse response = bookingService.initiateBooking(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getStatus());
        assertTrue(response.getMessage().contains("0x123"));
        assertTrue(response.getMessage().contains("test-hold-id"));
        assertTrue(response.getMessage().contains("400.0 ETH")); // 2 seats * 200.0
        verify(paymentService).processPaymentAsync(any(), any());
    }

    @Test
    void testSuccessfulNonCryptoBooking() {
        // Arrange
        testRequest.setPaymentMethod("CARD");
        when(showRepo.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatHoldService.areSeatsAvailable(any(), any())).thenReturn(true);
        when(seatHoldService.holdSeats(any(), any())).thenReturn("test-hold-id");

        // Act
        BookingResponse response = bookingService.initiateBooking(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(202, response.getStatus());
        assertTrue(response.getMessage().contains("test-hold-id"));
        verify(paymentService).processPaymentAsync(any(), any());
    }

    @Test
    void testShowNotFound() {
        // Arrange
        when(showRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            bookingService.initiateBooking(testRequest)
        );
    }

    @Test
    void testSeatsNotAvailable() {
        // Arrange
        when(showRepo.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatHoldService.areSeatsAvailable(any(), any())).thenReturn(false);

        // Act & Assert
        assertThrows(SeatAlreadyHeldException.class, () -> 
            bookingService.initiateBooking(testRequest)
        );
    }

    @Test
    void testGetBooking() {
        // Arrange
        String holdId = "test-hold-id";
        Booking booking = new Booking();
        booking.setHoldId(holdId);
        booking.setShow(testShow);
        booking.setSeats(Arrays.asList(new Seat(), new Seat()));
        booking.setPhoneNumber("1234567890");
        booking.setBookingTime(LocalDateTime.now());

        when(bookingRepo.findByHoldId(holdId)).thenReturn(Optional.of(booking));

        // Act
        TicketDTO ticket = bookingService.getBooking(holdId);

        // Assert
        assertNotNull(ticket);
        assertEquals(testShow.getShowId(), ticket.getShowId());
        assertEquals(testShow.getMovie().getMovieName(), ticket.getMovieName());
        assertEquals(testShow.getTheatre().getTheatreName(), ticket.getTheatreName());
        assertEquals(2, ticket.getSeats().size());
        assertEquals("1234567890", ticket.getPhoneNumber());
    }

    @Test
    void testGetBookingNotFound() {
        // Arrange
        when(bookingRepo.findByHoldId(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            bookingService.getBooking("non-existent-hold-id")
        );
    }
} 