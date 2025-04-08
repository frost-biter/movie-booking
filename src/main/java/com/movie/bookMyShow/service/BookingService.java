package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.PaymentFailedException;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyBookedException;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private ShowSeatRepo showSeatRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private PaymentService paymentService;
    @Transactional
    public Ticket bookSeats(BookingRequest request) throws InterruptedException {

        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        List<ShowSeat> showSeats = ShowSeatRepo.findForUpdate(show.getShowId(), request.getSeatIds());

        if(showSeats == null || showSeats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("Show seats not found");
        }

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == SeatStatus.BOOKED) {
                throw new SeatAlreadyBookedException("Seat " + ss.getSeat().getRow() + " " + ss.getSeat().getSeatNo() + " is already booked! ❌");
            }
            if (ss.getStatus() == SeatStatus.HELD && ss.getHoldUntil().isAfter(LocalDateTime.now())) {
                throw new SeatAlreadyBookedException("Seat " + ss.getSeat().getRow() + " " + ss.getSeat().getSeatNo() + " is currently held! ⏳");
            }
        }
        LocalDateTime holdUntil = LocalDateTime.now().plusMinutes(5);
        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.HELD);
            ss.setHoldUntil(holdUntil);
        }
        showSeatRepo.saveAll(showSeats); // Save held state

        boolean paymentSuccess = paymentService.processPaymentAndHoldSeats(showSeats);

        if (!paymentSuccess) {
            throw new PaymentFailedException("Payment failed! Please try again. ❌");
        }

        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.BOOKED);
            ss.setHoldUntil(null);
        }
        showSeatRepo.saveAll(showSeats);

        List<Seat> bookedSeats = showSeats.stream()
                .map(ShowSeat::getSeat)
                .toList();

        Ticket ticket = new Ticket();
        ticket.setShow(show);
        ticket.setSeats(bookedSeats);
        ticket.setPhoneNumber(request.getPhoneNumber());
        ticket.setBookingTime(LocalDateTime.now());

        return bookingRepo.save(ticket);
    }
}
