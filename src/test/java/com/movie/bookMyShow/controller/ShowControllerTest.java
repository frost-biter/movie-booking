package com.movie.bookMyShow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.bookMyShow.config.BaseControllerTestConfig;
import com.movie.bookMyShow.dto.SeatDTO;
import com.movie.bookMyShow.dto.ShowDTO;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.service.ShowService;
import com.movie.bookMyShow.service.ShowSeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShowController.class)
@Import(BaseControllerTestConfig.class)
class ShowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowService showService;

    @MockBean
    private ShowSeatService showSeatService;

    private ShowDTO testShow;
    private TheatreDTO testTheatre;
    private List<TheatreDTO> testTheatres;

    @BeforeEach
    void setUp() {
        // Setup test show
        testShow = new ShowDTO(
            1L,  // showId
            1L,  // movieId
            1L,  // theatreId
            1L,  // screenId
            LocalDateTime.now().plusHours(1),  // startTime
            LocalDateTime.now().plusHours(3)   // endTime
        );

        // Setup test theatre
        testTheatre = new TheatreDTO();
        testTheatre.setTheatreId(1L);
        testTheatre.setTheatreName("Test Theatre");
        testTheatre.setCity("Test City");
        testTheatre.setAddress("Test Address");
        testTheatre.setShows(Arrays.asList(testShow));

        // Setup test theatres list
        testTheatres = Arrays.asList(testTheatre);
    }

    @Test
    void testGetTheatresWithShows_Success() throws Exception {
        when(showService.getTheatresWithShows(anyInt(), anyInt())).thenReturn(testTheatres);

        mockMvc.perform(get("/shows/movie-id-1")
                .requestAttr("cityId", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].theatreId").value(1))
                .andExpect(jsonPath("$[0].theatreName").value("Test Theatre"))
                .andExpect(jsonPath("$[0].shows[0].showId").value(1));
    }

    @Test
    void testGetTheatresWithShows_NoCityId() throws Exception {
        mockMvc.perform(get("/shows/movie-id-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAvailableSeats_Success() throws Exception {
        Seat seat1 = new Seat();
        seat1.setSeatId(1L);
        seat1.setRow('A');
        seat1.setSeatNo(1L);
        
        Seat seat2 = new Seat();
        seat2.setSeatId(2L);
        seat2.setRow('A');
        seat2.setSeatNo(2L);

        List<Seat> mockSeats = Arrays.asList(seat1, seat2);
        when(showSeatService.getAvailableSeats(1L)).thenReturn(mockSeats);

        mockMvc.perform(get("/shows/show-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].seatId").value(1))
                .andExpect(jsonPath("$[0].row").value("A"))
                .andExpect(jsonPath("$[0].seatNo").value(1))
                .andExpect(jsonPath("$[1].seatId").value(2))
                .andExpect(jsonPath("$[1].row").value("A"))
                .andExpect(jsonPath("$[1].seatNo").value(2));
    }
} 