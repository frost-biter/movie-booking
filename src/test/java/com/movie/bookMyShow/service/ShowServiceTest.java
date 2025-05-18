package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.ShowDTO;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private MovieRepo movieRepo;

    @Mock
    private TheatreRepo theatreRepo;

    @Mock
    private ScreenRepo screenRepo;

    @Mock
    private ShowRepo showRepo;

    @InjectMocks
    private ShowService showService;

    private ShowRequest showRequest;
    private Movie movie;
    private Theatre theatre;
    private Screen screen;
    private Show show;

    @BeforeEach
    void setUp() {
        // Setup test data
        movie = new Movie();
        movie.setMovieId(1);
        movie.setMovieName("Test Movie");

        theatre = new Theatre();
        theatre.setTheatreId(1L);
        theatre.setTheatreName("Test Theatre");

        screen = new Screen();
        screen.setScreenId(1L);
        screen.setTheatre(theatre);

        show = new Show();
        show.setShowId(1L);
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreen(screen);
        show.setStartTime(LocalDateTime.now().plusHours(2));
        show.setEndTime(LocalDateTime.now().plusHours(4));

        theatre.setShowList(Arrays.asList(show));

        showRequest = new ShowRequest();
        showRequest.setMovieId(1);
        showRequest.setTheatreId(1);
        showRequest.setScreenId(1);
        showRequest.setStartTime(LocalDateTime.now().plusHours(2));
    }

    @Test
    void addShow_Success() {
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(movie));
        when(theatreRepo.findById(1L)).thenReturn(java.util.Optional.of(theatre));
        when(screenRepo.findById(1L)).thenReturn(java.util.Optional.of(screen));
        when(showRepo.existsOverlappingShow(any(), any(), any(), any())).thenReturn(false);
        when(showRepo.save(any(Show.class))).thenReturn(show);

        ApiResponse response = showService.addShow(showRequest);

        assertEquals(201, response.getStatus());
        assertEquals("Show added successfully", response.getMessage());
        verify(showRepo).save(any(Show.class));
    }

    @Test
    void addShow_WithPastTime_ShouldThrowException() {
        showRequest.setStartTime(LocalDateTime.now().minusHours(1));
        
        // Mock the repository calls to avoid ResourceNotFoundException
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(movie));
        when(theatreRepo.findById(1L)).thenReturn(java.util.Optional.of(theatre));
        when(screenRepo.findById(1L)).thenReturn(java.util.Optional.of(screen));

        assertThrows(IllegalArgumentException.class, () -> showService.addShow(showRequest));
    }

    @Test
    void addShow_WithOverlappingShow_ShouldReturnConflict() {
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(movie));
        when(theatreRepo.findById(1L)).thenReturn(java.util.Optional.of(theatre));
        when(screenRepo.findById(1L)).thenReturn(java.util.Optional.of(screen));
        when(showRepo.existsOverlappingShow(any(), any(), any(), any())).thenReturn(true);

        ApiResponse response = showService.addShow(showRequest);

        assertEquals(409, response.getStatus());
        assertEquals("Show is Overlapping", response.getMessage());
    }

    @Test
    void getTheatresWithShows_Success() {
        List<Theatre> theatres = Arrays.asList(theatre);
        when(theatreRepo.findByCityAndMovie(anyInt(), anyInt())).thenReturn(theatres);
        when(movieRepo.findMovieNameByMovieId(anyInt())).thenReturn("Test Movie");

        List<TheatreDTO> result = showService.getTheatresWithShows(1, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Theatre", result.get(0).getTheatreName());
    }

    @Test
    void addShow_WithInvalidMovie_ShouldThrowException() {
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showService.addShow(showRequest));
    }

    @Test
    void addShow_WithInvalidTheatre_ShouldThrowException() {
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(movie));
        when(theatreRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showService.addShow(showRequest));
    }

    @Test
    void addShow_WithInvalidScreen_ShouldThrowException() {
        when(movieRepo.findById(1L)).thenReturn(java.util.Optional.of(movie));
        when(theatreRepo.findById(1L)).thenReturn(java.util.Optional.of(theatre));
        when(screenRepo.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showService.addShow(showRequest));
    }
} 