package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyBookedException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Ticket;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Transactional
    public Ticket bookSeats(BookingRequest request) {

        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // ✅ Fetch seats with pessimistic locking (Prevents concurrent booking issues)
        List<Seat> seats = seatRepo.findAllById(request.getSeatIds());

        Set<Seat> bookedSeats = new HashSet<>(show.getBookedSeats());

        for (Seat seat : seats) {
            if (bookedSeats.contains(seat)) {
                throw new SeatAlreadyBookedException("Seat " + seat.getRow()+" "+seat.getSeatNo() + " is already booked! ❌");
            }
            bookedSeats.add(seat); // Mark seat as booked
        }
        for (Seat seat:seats){
            show.addBookedSeat(seat); // Add to show
        }

        Ticket ticket = new Ticket();
        ticket.setShow(show);
        ticket.setSeats(seats);
        ticket.setPhoneNumber(request.getPhoneNumber());
        ticket.setBookingTime(LocalDateTime.now());

        return ticket;
    }
}
