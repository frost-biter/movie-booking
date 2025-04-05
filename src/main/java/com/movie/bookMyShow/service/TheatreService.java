package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.model.Theatre;
import com.movie.bookMyShow.repo.TheatreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TheatreService {
    @Autowired
    private TheatreRepo theatreRepo;
    public ApiResponse addTheatre(Theatre theatre) {
        if (theatreRepo.existsByTheatreName(theatre.getTheatreName())) {
            return new ApiResponse(409, "Theatre already Exists");
        }
        theatreRepo.save(theatre);
        return new ApiResponse(201, "Theatre added successfully");
    }
}
