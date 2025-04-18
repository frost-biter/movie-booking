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
    private ShowSeatRepo showSeatRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Transactional
    public void clearAllData() {
        bookingRepo.deleteAll();
        showSeatRepo.deleteAll();
        seatRepo.deleteAll();
        showRepo.deleteAll();
        screenRepo.deleteAll();
        theatreRepo.deleteAll();
        movieRepo.deleteAll();
    }

    public Movie createMovie(String name, int duration) {
        Movie movie = new Movie();
        movie.setMovieName(name);
        movie.setDuration(duration);
        return movieRepo.save(movie);
    }

    public Theatre createTheatre(String name) {
        Theatre theatre = new Theatre();
        theatre.setTheatreName(name);
        return theatreRepo.save(theatre);
    }

    public Screen createScreen(Theatre theatre, String name) {
        Screen screen = new Screen();
        screen.setScreenName(name);
        screen.setTheatre(theatre);
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
        for (int i = 0; i < count; i++) {
            Seat seat = new Seat();
            seat.setScreen(screen);
            seat.setRow('A');
            seat.setSeatNo((long) (i + 1));
            seats.add(seatRepo.save(seat));
        }
        return seats;
    }
} 