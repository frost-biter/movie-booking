package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.PaymentFailedException;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.*;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    @Autowired
    private SeatHoldService seatHoldService;

    @Transactional
    public Ticket bookSeats(BookingRequest request) {
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // 1. Hold seats in Redis
        String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());

        try {
            // 2. Process payment
            boolean paymentSuccess = paymentService.processPayment(request);
            if (!paymentSuccess) {
                throw new PaymentFailedException("Payment failed! Please try again. ❌");
            }

            // 3. Create ShowSeat entries for booked seats
            List<ShowSeat> showSeats = request.getSeatIds().stream()
                .map(seatId -> {
                    ShowSeat showSeat = new ShowSeat();
                    showSeat.setShow(show);
                    showSeat.setSeatId(seatId);
                    showSeat.setStatus(SeatStatus.BOOKED);
                    return showSeat;
                })
                .toList();

            showSeatRepo.saveAll(showSeats);

            // 4. Create ticket
            Ticket ticket = new Ticket();
            ticket.setShow(show);
            ticket.setSeats(showSeats.stream().map(ShowSeat::getSeat).toList());
            ticket.setPhoneNumber(request.getPhoneNumber());
            ticket.setBookingTime(LocalDateTime.now());

            return bookingRepo.save(ticket);
        } catch (Exception e) {
            // Release holds if anything fails
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
            throw e;
        }
    }
}
