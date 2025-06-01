package com.movie.bookMyShow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.bookMyShow.config.BaseControllerTestConfig;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.service.TheatreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TheatreController.class)
@Import(BaseControllerTestConfig.class)
class TheatreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TheatreService theatreService;

    private TheatreDTO testTheatre;
    private List<TheatreDTO> testTheatres;

    @BeforeEach
    void setUp() {
        // Setup test theatre
        testTheatre = new TheatreDTO();
        testTheatre.setTheatreId(1L);
        testTheatre.setTheatreName("Test Theatre");
        testTheatre.setCity("Test City");
        testTheatre.setAddress("Test Address");

        // Setup test theatres list
        testTheatres = Arrays.asList(testTheatre);
    }

    @Test
    void testGetAllTheatres_Success() throws Exception {
        when(theatreService.getAllTheatres()).thenReturn(testTheatres);

        mockMvc.perform(get("/api/theatres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].theatreId").value(1))
                .andExpect(jsonPath("$[0].theatreName").value("Test Theatre"));
    }

    @Test
    void testGetTheatreById_Success() throws Exception {
        when(theatreService.getTheatreById(1L)).thenReturn(testTheatre);

        mockMvc.perform(get("/api/theatres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theatreId").value(1))
                .andExpect(jsonPath("$.theatreName").value("Test Theatre"));
    }

    @Test
    void testGetTheatreById_NotFound() throws Exception {
        when(theatreService.getTheatreById(999L))
                .thenThrow(new ResourceNotFoundException("Theatre not found"));

        mockMvc.perform(get("/api/theatres/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTheatresByCity_Success() throws Exception {
        when(theatreService.getTheatresByCity("Test City")).thenReturn(testTheatres);

        mockMvc.perform(get("/api/theatres/city/Test City"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].theatreId").value(1))
                .andExpect(jsonPath("$[0].theatreName").value("Test Theatre"));
    }
} 