package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {
    @Autowired
    private SeatHoldService seatHoldService;
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private ShowSeatRepo showSeatRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPaymentAsync(String holdId, BookingRequest request) {
        try {
            // Validate the hold is still valid
            if (!seatHoldService.validateHold(request.getShowId(), holdId, request.getSeatIds())) {
                log.error("Hold validation failed for holdId: {}", holdId);
                return;
            }

            boolean paymentSuccess = processPaymentWithGateway(request);

            if (paymentSuccess) {
                try {
                    TicketDTO ticket = createBookingAndGetTicket(request, holdId);
                    log.info("Ticket generated successfully: {}", ticket);
                } catch (Exception e) {
                    log.error("Failed to create booking: {}", e.getMessage());
                    seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
                    throw e;
                }
            } else {
                log.info("Payment failed, releasing hold: {}", holdId);
                seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
            }
        } catch (Exception e) {
            log.error("Error in payment processing: {}", e.getMessage());
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private TicketDTO createBookingAndGetTicket(BookingRequest request, String holdId) {
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Validate the hold is still valid
        if (!seatHoldService.validateHold(request.getShowId(), holdId, request.getSeatIds())) {
            throw new IllegalStateException("Seats are no longer available");
        }

        // Create permanent booking
        List<ShowSeat> showSeats = request.getSeatIds().stream()
                .<ShowSeat>map(seatId -> {
                    com.movie.bookMyShow.model.Seat seat = seatRepo.findById(seatId)
                            .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
                    
                    return ShowSeat.builder()
                            .show(show)
                            .seat(seat)
                            .status(SeatStatus.BOOKED)
                            .build();
                })
                .collect(Collectors.toList());

        try {
            List<ShowSeat> savedSeats = showSeatRepo.saveAll(showSeats);
            log.info("Successfully saved {} show seats", savedSeats.size());
        } catch (Exception e) {
            log.error("Failed to save show seats: {}", e.getMessage());
            throw new RuntimeException("Failed to create booking", e);
        }

        return new TicketDTO(
                show.getShowId(),
                show.getMovie().getMovieName(),
                show.getTheatre().getTheatreName(),
                show.getStartTime(),
                seatRepo.findAllById(request.getSeatIds()),
                request.getPhoneNumber(),
                LocalDateTime.now()
        );
    }

    private boolean processPaymentWithGateway(BookingRequest request) throws InterruptedException {
        log.info("Processing payment for request: {}", request);
        // Implement actual payment gateway integration here
        Thread.sleep(10000);
        boolean success = Math.random() > 0.1; // 90% success rate
        log.info("Payment processing result: {}", success);
        return success;
    }
}
