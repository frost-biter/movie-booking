package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.SeatDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowSeatService {
    @Autowired
    private ShowSeatRepo showSeatRepo;
    @Autowired
    private ShowRepo showRepo;

    public List<SeatDTO> getAvailableSeats(Long showId) {
        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        Long screenId = show.getScreen().getScreenId();

        List<Seat> seats = showSeatRepo.findAvailableSeats(showId, screenId);
        
        return seats.stream()
                .map(seat -> new SeatDTO(
                    seat.getSeatId(),
                    seat.getRow(),
                    seat.getSeatNo(),
                    seat.getCategory(),
                    seat.getRow() + String.valueOf(seat.getSeatNo())
                ))
                .collect(Collectors.toList());
    }
}
