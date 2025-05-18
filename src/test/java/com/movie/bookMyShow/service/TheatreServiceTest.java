package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.model.City;
import com.movie.bookMyShow.model.Theatre;
import com.movie.bookMyShow.repo.TheatreRepo;
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
class TheatreServiceTest {

    @Mock
    private TheatreRepo theatreRepo;

    @InjectMocks
    private TheatreService theatreService;

    private Theatre theatre;
    private City city;

    @BeforeEach
    void setUp() {
        city = new City();
        city.setCityId(1);
        city.setCityName("Test City");

        theatre = new Theatre();
        theatre.setTheatreId(1L);
        theatre.setTheatreName("Test Theatre");
        theatre.setCity(city);
        theatre.setAddress("Test Address");
    }

    @Test
    void addTheatre_Success() {
        when(theatreRepo.existsByTheatreName(any())).thenReturn(false);
        when(theatreRepo.save(any(Theatre.class))).thenReturn(theatre);

        ApiResponse response = theatreService.addTheatre(theatre);

        assertEquals(201, response.getStatus());
        assertEquals("Theatre added successfully", response.getMessage());
        verify(theatreRepo).save(theatre);
    }

    @Test
    void addTheatre_WhenTheatreExists_ShouldReturnConflict() {
        when(theatreRepo.existsByTheatreName(any())).thenReturn(true);

        ApiResponse response = theatreService.addTheatre(theatre);

        assertEquals(409, response.getStatus());
        assertEquals("Theatre already Exists", response.getMessage());
        verify(theatreRepo, never()).save(any(Theatre.class));
    }
} 