package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.ShowDTO;
import com.movie.bookMyShow.dto.ShowRequest;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Movie;
import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Theatre;
import com.movie.bookMyShow.repo.MovieRepo;
import com.movie.bookMyShow.repo.ScreenRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.TheatreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public ApiResponse addShow(ShowRequest request) {
        Movie movie = movieRepo.findById((long) request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Theatre theatre = theatreRepo.findById((long) request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

        Screen screen = screenRepo.findById((long) request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        // Verify screen belongs to the specified theatre
        if (!screen.getTheatre().getTheatreId().equals(theatre.getTheatreId())) {
            throw new IllegalArgumentException(
                    "Screen " + screen.getScreenId() +
                            " does not belong to Theatre " + theatre.getTheatreId()
            );
        }

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

    public List<TheatreDTO> getTheatresWithShows(int movieId, Integer cityId) {
        List<Theatre> theatres = theatreRepo.findByCityAndMovie(cityId, movieId);

        return theatres.stream().map(theatre -> {
            TheatreDTO theatreDTO = new TheatreDTO();
            theatreDTO.setTheatreId(theatre.getTheatreId());
            theatreDTO.setTheatreName(theatre.getTheatreName());
            theatreDTO.setAddress(theatre.getAddress());
            theatreDTO.setMovieName(movieRepo.findMovieNameByMovieId(movieId));

            List<ShowDTO> showDTOs = theatre.getShowList().stream()
                    .filter(show -> show.getMovie().getMovieId() == movieId) // Only get shows for the selected movie
                    .map(show -> new ShowDTO(Math.toIntExact(show.getShowId()),show.getStartTime(), show.getEndTime()))
                    .collect(Collectors.toList());

            theatreDTO.setShows(showDTOs);
            return theatreDTO;
        }).collect(Collectors.toList());
    }

}