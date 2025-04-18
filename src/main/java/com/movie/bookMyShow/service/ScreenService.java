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
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScreenService {
    @Autowired
    private ScreenRepo screenRepo;
    @Autowired
    private TheatreRepo theatreRepo;

    @Transactional
    public ApiResponse addScreen(Screen screen) {
        try {
            // Validate screen has theatre information
            if (screen.getTheatre() == null || screen.getTheatre().getTheatreId() == null) {
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), 
                    "Theatre information is required for adding a screen");
            }

            // Validate theatre exists
            Theatre theatre = theatreRepo.findById(screen.getTheatre().getTheatreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

            // Check if screen already exists in this theatre
            if (screen.getScreenId() != null && 
                screenRepo.existsByScreenIdAndTheatre(screen.getScreenId(), theatre)) {
                return new ApiResponse(HttpStatus.CONFLICT.value(), 
                    "Screen " + screen.getScreenId() + " already exists in theatre " + theatre.getTheatreId());
            }

            // Set the theatre and save
            screen.setTheatre(theatre);
            Screen savedScreen = screenRepo.save(screen);
            
            return new ApiResponse(HttpStatus.CREATED.value(), 
                "Screen added successfully with ID: " + savedScreen.getScreenId());
        } catch (ResourceNotFoundException e) {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            return new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "Error adding screen: " + e.getMessage());
        }
    }
}