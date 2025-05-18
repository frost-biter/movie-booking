package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.ShowDTO;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowService {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private TheatreRepo theatreRepo;

    @Autowired
    private ScreenRepo screenRepo;

    @Autowired
    private ShowRepo showRepo;

    @Transactional
    public ApiResponse addShow(ShowRequest request) {
        validateShowRequest(request);

        Movie movie = movieRepo.findById((long) request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Theatre theatre = theatreRepo.findById((long) request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

        Screen screen = screenRepo.findById((long) request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        validateScreenTheatreRelationship(screen, theatre);
        validateShowTime(request.getStartTime());

        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreen(screen);
        show.setStartTime(request.getStartTime());
        boolean isOverlapping = showRepo.existsOverlappingShow(theatre,screen,show.getStartTime(),show.getEndTime());
        if(isOverlapping){
            return new ApiResponse(409, "Show is Overlapping");
        }

        showRepo.save(show);

        return new ApiResponse(201, "Show added successfully");
    }
    

    private void validateShowRequest(ShowRequest request) {
        if (request == null || request.getStartTime() == null) {
            throw new IllegalArgumentException("Invalid show request");
        }
    }

    private void validateShowTime(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isBefore(now)) {
            throw new IllegalArgumentException("Show time cannot be in the past");
        }
        
        // Allow shows up to 6 months in advance
        LocalDateTime maxFutureDate = now.plusMonths(6);
        if (startTime.isAfter(maxFutureDate)) {
            throw new IllegalArgumentException("Show time cannot be more than 6 months in advance");
        }
    }

    private void validateScreenTheatreRelationship(Screen screen, Theatre theatre) {
        if (!screen.getTheatre().getTheatreId().equals(theatre.getTheatreId())) {
            throw new IllegalArgumentException(
                    "Screen " + screen.getScreenId() +
                    " does not belong to Theatre " + theatre.getTheatreId()
            );
        }
    }

    public List<TheatreDTO> getTheatresWithShows(int movieId, Integer cityId) {
        List<Theatre> theatres = theatreRepo.findByCityAndMovie(cityId, movieId);

        return theatres.stream().map(theatre -> {
            TheatreDTO theatreDTO = new TheatreDTO();
            theatreDTO.setTheatreId(theatre.getTheatreId());
            theatreDTO.setTheatreName(theatre.getTheatreName());
            theatreDTO.setAddress(theatre.getAddress());
            theatreDTO.setMovieName(movieRepo.findMovieNameByMovieId(movieId));

            List<ShowDTO> showDTOs = theatre.getShowList().stream()
                    .filter(show -> show.getMovie().getMovieId() == movieId && 
                           show.getStartTime().isAfter(LocalDateTime.now())) // Only get future shows
                    .map(show -> new ShowDTO(
                        show.getShowId(),
                        Long.valueOf(show.getMovie().getMovieId()),
                        show.getTheatre().getTheatreId(),
                        show.getScreen().getScreenId(),
                        show.getStartTime(),
                        show.getEndTime()
                    ))
                    .collect(Collectors.toList());

            theatreDTO.setShows(showDTOs);
            return theatreDTO;
        }).collect(Collectors.toList());
    }

}