package com.movie.bookMyShow.service;

import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.model.ShowSeat;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    public boolean processPaymentAndHoldSeats(List<ShowSeat> showSeats) {
        try {
            // Simulate payment delay (optional)
            Thread.sleep(1000); // 1 sec delay

            // Simulate success (90% chance)
            return Math.random() < 0.9;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
