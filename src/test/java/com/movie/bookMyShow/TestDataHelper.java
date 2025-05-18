package com.movie.bookMyShow;

import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestDataHelper {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private TheatreRepo theatreRepo;

    @Autowired
    private ScreenRepo screenRepo;

    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private SeatRepo seatRepo;

    @Autowired
    private CityRepo cityRepo;

    @Transactional
    public void clearAllData() {
        showRepo.deleteAll();
        seatRepo.deleteAll();
        screenRepo.deleteAll();
        theatreRepo.deleteAll();
        movieRepo.deleteAll();
        cityRepo.deleteAll();
    }

    public Movie createMovie(String name, int duration) {
        Movie movie = new Movie();
        movie.setMovieName(name);
        movie.setDuration(duration);
        return movieRepo.save(movie);
    }

    public City createCity(String name) {
        City city = new City();
        city.setCityName(name);
        return cityRepo.save(city);
    }

    public Theatre createTheatre(String name) {
        Theatre theatre = new Theatre();
        theatre.setTheatreName(name);
        theatre.setAddress("Test Address");
        theatre.setCity(createCity("Test City"));
        return theatreRepo.save(theatre);
    }

    public Screen createScreen(Theatre theatre, String screenName) {
        Screen screen = new Screen();
        screen.setTheatre(theatre);
        screen.setScreenName(screenName);
        return screenRepo.save(screen);
    }

    public Show createShow(Movie movie, Theatre theatre, Screen screen, LocalDateTime startTime) {
        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setScreen(screen);
        show.setStartTime(startTime);
        return showRepo.save(show);
    }

    public List<Seat> createSeats(Screen screen, int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat();
            seat.setScreen(screen);
            seat.setRow('A');
            seat.setSeatNo((long) i);
            seats.add(seatRepo.save(seat));
        }
        return seats;
    }
} 