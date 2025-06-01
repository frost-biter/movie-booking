package com.movie.bookMyShow.config;

import com.movie.bookMyShow.service.SeatHoldService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SeatHoldServiceTestConfig {
    
    @Bean
    public SeatHoldService seatHoldService() {
        return new SeatHoldService();
    }
} 