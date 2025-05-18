package com.movie.bookMyShow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.bookMyShow.config.BaseControllerTestConfig;
import com.movie.bookMyShow.dto.MovieDTO;
import com.movie.bookMyShow.model.Movie;
import com.movie.bookMyShow.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@Import(BaseControllerTestConfig.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    private Movie testMovie;
    private List<Movie> testMovies;

    @BeforeEach
    void setUp() {
        // Setup test movie
        testMovie = new Movie();
        testMovie.setMovieId(1);
        testMovie.setMovieName("Test Movie");
        testMovie.setDuration(120);

        // Setup test movies list
        testMovies = Arrays.asList(testMovie);
    }

    @Test
    void testGetMoviesByCity_Success() throws Exception {
        when(movieService.getMovies(anyInt())).thenReturn(testMovies);

        mockMvc.perform(get("/movies/list")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].movieId").value(1))
                .andExpect(jsonPath("$[0].movieName").value("Test Movie"));
    }

    @Test
    void testGetMoviesByCity_Unauthorized() throws Exception {
        mockMvc.perform(get("/movies/list"))
                .andExpect(status().isUnauthorized());
    }
} 