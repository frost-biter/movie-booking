package com.movie.bookMyShow.service;

import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowSeatService {
    @Autowired
    private ShowSeatRepo showSeatRepo;
    @Autowired
    private ShowRepo showRepo;
    public List<Seat> getAvailableSeats(Long showId) {
        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        Long screenId = show.getScreen().getScreenId();

        return showSeatRepo.findAvailableSeats(showId, screenId);
    }
}
