package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.repo.SeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SeatService {

    @Autowired
    private SeatRepo seatRepo;
    public ApiResponse addSeat(Seat seat) {
        try {
            Long screenId = seat.getScreen().getScreenId();
            Character row = seat.getRow();
            Long seatNo = seat.getSeatNo();

            boolean alreadyExists = seatRepo.existsByScreen_ScreenIdAndRowAndSeatNo(screenId, row, seatNo);

            if (alreadyExists) {
                return new ApiResponse(HttpStatus.CONFLICT.value(), "Seat already exists in this screen");
            }

            Seat savedSeat = seatRepo.save(seat);
            return new ApiResponse(HttpStatus.CREATED.value(), "Seat added successfully: " + savedSeat.getSeatId());

        } catch (Exception e) {
            return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error adding seat: " + e.getMessage());
        }
    }

}