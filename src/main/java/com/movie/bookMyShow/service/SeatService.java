package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.enums.SeatCategory;
import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeatService {

    @Autowired
    private ShowRepo showRepo;

    public List<Seat> getAvailableSeats(Long showId) {

        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));
        Set<Seat> bookedSeatsSet = new HashSet<>(show.getBookedSeats()); // ✅ Convert to normal Set

        List<Seat> seats = new ArrayList<>(show.getScreen().getSeats().stream()
                .filter(seat -> !bookedSeatsSet.contains(seat)) // ✅ No Hibernate issues now
                .toList());

        if (!seats.isEmpty()) System.out.print("Available seats: " + seats.size());
        else System.out.print("No available seats");
        return seats;
    }


    @Autowired
    private SeatRepo seatRepo;

//    public ApiResponse addSeat(Seat seat) {
//        try {
//            Seat savedSeat = seatRepo.save(seat);
//            return new ApiResponse(HttpStatus.CREATED.value(), "Seat added successfully"+ savedSeat.getSeatId());
//        } catch (Exception e) {
//            return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "  adding seat: " + e.getMessage());
//        }
//    }
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