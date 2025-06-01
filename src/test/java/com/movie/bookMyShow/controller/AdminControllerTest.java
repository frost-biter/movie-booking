package com.movie.bookMyShow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.bookMyShow.config.BaseControllerTestConfig;
import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.CredentialsRequest;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.service.*;
import com.movie.bookMyShow.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(BaseControllerTestConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @MockBean
    private ShowService showService;

    @MockBean
    private CityService cityService;

    @MockBean
    private TheatreService theatreService;

    @MockBean
    private ScreenService screenService;

    @MockBean
    private SeatService seatService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    private Movie testMovie;
    private ShowRequest testShowRequest;
    private City testCity;
    private Theatre testTheatre;
    private Screen testScreen;
    private Seat testSeat;
    private CredentialsRequest testCredentials;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        // Setup test movie
        testMovie = new Movie();
        testMovie.setMovieId(1);
        testMovie.setMovieName("Test Movie");
        testMovie.setDuration(120);

        // Setup test show request
        testShowRequest = new ShowRequest();
        testShowRequest.setMovieId(1);
        testShowRequest.setTheatreId(1);
        testShowRequest.setScreenId(1);
        testShowRequest.setStartTime(LocalDateTime.now().plusHours(2));

        // Setup test city
        testCity = new City();
        testCity.setCityId(1);
        testCity.setCityName("Test City");

        // Setup test theatre
        testTheatre = new Theatre();
        testTheatre.setTheatreId(1L);
        testTheatre.setTheatreName("Test Theatre");
        testTheatre.setCity(testCity);

        // Setup test screen
        testScreen = new Screen();
        testScreen.setScreenId(1L);
        testScreen.setScreenName("Screen 1");
        testScreen.setTheatre(testTheatre);

        // Setup test seat
        testSeat = new Seat();
        testSeat.setSeatId(1L);
        testSeat.setRow('A');
        testSeat.setSeatNo(1L);
        testSeat.setScreen(testScreen);

        // Setup test credentials
        testCredentials = new CredentialsRequest();
        testCredentials.setUsername("testadmin");
        testCredentials.setPassword("password123");

        // Setup test admin
        testAdmin = new Admin();
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword("encodedPassword");
        testAdmin.setRole("ADMIN");
    }

    @Test
    void testRegisterAdmin_Success() throws Exception {
        when(adminService.register(any(CredentialsRequest.class)))
            .thenReturn("Admin registered successfully");

        mockMvc.perform(post("/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCredentials)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Admin registered successfully"));
    }

    @Test
    void testLoginAdmin_Success() throws Exception {
        // Mock admin service to return test admin
        when(adminService.findByUsername("testadmin")).thenReturn(testAdmin);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAdminToken(anyString(), anyString())).thenReturn("test-token");

        mockMvc.perform(post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCredentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void testLoginAdmin_InvalidCredentials() throws Exception {
        when(adminService.findByUsername("testadmin")).thenReturn(null);

        mockMvc.perform(post("/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void testAddMovie_Success() throws Exception {
        ApiResponse response = new ApiResponse(201, "Movie added successfully");
        when(movieService.addMovie(any(Movie.class))).thenReturn(response);

        mockMvc.perform(post("/admin/add-movie")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMovie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Movie added successfully"));
    }

    @Test
    void testAddShow_Success() throws Exception {
        ApiResponse response = new ApiResponse(201, "Show added successfully");
        when(showService.addShow(any(ShowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/add-show")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testShowRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Show added successfully"));
    }

    @Test
    void testAddCity_Success() throws Exception {
        mockMvc.perform(post("/admin/add-city")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCity)))
                .andExpect(status().isCreated())
                .andExpect(content().string("City added successfully"));
    }

    @Test
    void testAddTheatre_Success() throws Exception {
        ApiResponse response = new ApiResponse(201, "Theatre added successfully");
        when(theatreService.addTheatre(any(Theatre.class))).thenReturn(response);

        mockMvc.perform(post("/admin/add-theatre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTheatre)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Theatre added successfully"));
    }

    @Test
    void testAddScreen_Success() throws Exception {
        ApiResponse response = new ApiResponse(201, "Screen added successfully");
        when(screenService.addScreen(any(Screen.class))).thenReturn(response);

        mockMvc.perform(post("/admin/add-screen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testScreen)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Screen added successfully"));
    }

    @Test
    void testAddSeat_Success() throws Exception {
        ApiResponse response = new ApiResponse(201, "Seat added successfully");
        when(seatService.addSeat(any(Seat.class))).thenReturn(response);

        mockMvc.perform(post("/admin/add-seat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSeat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Seat added successfully"));
    }
} 