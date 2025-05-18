package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.model.Movie;
import com.movie.bookMyShow.repo.MovieRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepo movieRepo;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setMovieId(1);
        movie.setMovieName("Test Movie");
    }

    @Test
    void addMovie_Success() {
        when(movieRepo.existsByMovieName(any())).thenReturn(false);
        when(movieRepo.save(any(Movie.class))).thenReturn(movie);

        ApiResponse response = movieService.addMovie(movie);

        assertEquals(201, response.getStatus());
        assertEquals("Movie added successfully", response.getMessage());
        verify(movieRepo).save(movie);
    }

    @Test
    void addMovie_WhenMovieExists_ShouldReturnConflict() {
        when(movieRepo.existsByMovieName(any())).thenReturn(true);

        ApiResponse response = movieService.addMovie(movie);

        assertEquals(409, response.getStatus());
        assertEquals("Movie already Exists", response.getMessage());
        verify(movieRepo, never()).save(any(Movie.class));
    }

    @Test
    void getMovies_Success() {
        List<Movie> expectedMovies = Arrays.asList(movie);
        when(movieRepo.findMoviesByCityId(anyInt())).thenReturn(expectedMovies);

        List<Movie> result = movieService.getMovies(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getMovieName());
        verify(movieRepo).findMoviesByCityId(1);
    }

    @Test
    void getMovies_WhenNoMovies_ShouldReturnEmptyList() {
        when(movieRepo.findMoviesByCityId(anyInt())).thenReturn(Arrays.asList());

        List<Movie> result = movieService.getMovies(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(movieRepo).findMoviesByCityId(1);
    }
} 