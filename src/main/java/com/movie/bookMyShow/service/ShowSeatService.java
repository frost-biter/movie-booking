package com.movie.bookMyShow.service;

import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowSeatService {
    @Autowired
    private ShowSeatRepo showSeatRepo;
    public List<Seat> getAvailableSeats(Long showId) {
        return showSeatRepo.findAvailableSeatsByShowId(showId);
    }
}
