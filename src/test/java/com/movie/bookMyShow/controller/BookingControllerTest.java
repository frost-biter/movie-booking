package com.movie.bookMyShow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.bookMyShow.config.BaseControllerTestConfig;
import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.BookingResponse;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(BaseControllerTestConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequest testRequest;
    private BookingResponse testResponse;
    private TicketDTO testTicket;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = new BookingRequest();
        testRequest.setShowId(1L);
        testRequest.setSeatIds(Arrays.asList(1L, 2L));
        testRequest.setPaymentMethod("ETH");
        testRequest.setPhoneNumber("1234567890");
        testRequest.setPrice(400.0);

        // Setup test response
        testResponse = new BookingResponse("Send payment to this ETH address",": 0x123." ,"Hold ID: test-hold-id.",null, 400.0);

        // Setup test ticket
        testTicket = new TicketDTO();
        testTicket.setShowId(1L);
        testTicket.setMovieName("Test Movie");
        testTicket.setTheatreName("Test Theatre");
        testTicket.setSeats(Arrays.asList(new Seat(), new Seat()));
        testTicket.setPhoneNumber("1234567890");
    }

    @Test
    void testInitiateBooking_Success() throws Exception {
        when(bookingService.initiateBooking(any(BookingRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/booking/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value(testResponse.getMessage()));
    }

    @Test
    void testInitiateBooking_InvalidRequest() throws Exception {
        testRequest.setShowId(null);

        mockMvc.perform(post("/booking/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBooking_Success() throws Exception {
        when(bookingService.getBooking("test-hold-id")).thenReturn(testTicket);

        mockMvc.perform(get("/booking/bookings")
                .param("holdId", "test-hold-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showId").value(1))
                .andExpect(jsonPath("$.movieName").value("Test Movie"))
                .andExpect(jsonPath("$.theatreName").value("Test Theatre"))
                .andExpect(jsonPath("$.seats").isArray())
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    void testGetBooking_NotFound() throws Exception {
        when(bookingService.getBooking("invalid-hold-id"))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/booking/bookings")
                .param("holdId", "invalid-hold-id"))
                .andExpect(status().isNotFound());
    }
} 