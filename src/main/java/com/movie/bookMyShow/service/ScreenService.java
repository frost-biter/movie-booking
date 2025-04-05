package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Theatre;
import com.movie.bookMyShow.repo.ScreenRepo;
import com.movie.bookMyShow.repo.TheatreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ScreenService {
    @Autowired
    private ScreenRepo screenRepo;
    @Autowired
    private TheatreRepo theatreRepo;
    public ApiResponse addScreen(Screen screen) {
        try {
            Theatre theatre = theatreRepo.findById((long) screen.getTheatre().getTheatreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));
            if (screen.getScreenId() != 0 &&
                    !screenRepo.existsByScreenIdAndTheatre(screen.getScreenId(), theatre)) {
                throw new IllegalArgumentException("Screen belongs to a different theatre");
            }
            screen.setTheatre(theatre); // Force consistency
            Screen savedScreen = screenRepo.save(screen);
            return new ApiResponse(HttpStatus.CREATED.value(), "Screen added successfully :"+ savedScreen.getScreenId());
        } catch (Exception e) {
            return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error adding screen: " + e.getMessage());
        }
    }
}