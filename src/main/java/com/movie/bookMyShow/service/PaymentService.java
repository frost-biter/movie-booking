package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private KafkaTemplate<String , TicketDTO> kafkaBookMovieTemplate;

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
                    kafkaBookMovieTemplate.send("book_movie", holdId , ticket);
                    log.info("Produced message: {}", ticket);
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
            // Check if seats are still available and not booked
            if (seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds())) {
                log.info("Hold expired but seats are still available, creating new hold");
                // Try to get a new hold
                String newHoldId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());
                if (newHoldId != null) {
                    // Create booking with new hold
                    return createBookingAndGetTicket(request, newHoldId);
                }
            }
            // If seats are not available, we need to initiate payment reversal
            log.error("Seats are no longer available after payment, initiating payment reversal");
            revertPayment(request);
            throw new IllegalStateException("Seats are no longer available after payment");
        }

        // Create permanent booking
        List<ShowSeat> showSeats = request.getSeatIds().stream()
                .map(seatId -> {
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
            // Save show seats first
            List<ShowSeat> savedSeats = showSeatRepo.saveAll(showSeats);
            log.info("Successfully saved {} show seats", savedSeats.size());

            // Create and save booking
            Booking booking = new Booking();
            booking.setHoldId(holdId);
            booking.setShow(show);
            booking.setSeats(savedSeats.stream().map(ShowSeat::getSeat).collect(Collectors.toList()));
            booking.setPrice(calculatePrice(savedSeats.size()));
            booking.setPhoneNumber(request.getPhoneNumber());
            booking.setBookingTime(LocalDateTime.now());

            bookingRepo.save(booking);
            log.info("Successfully created booking with hold ID: {}", holdId);

            // Release the hold since booking is successful
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());

            return new TicketDTO(
                    show.getShowId(),
                    show.getMovie().getMovieName(),
                    show.getTheatre().getTheatreName(),
                    show.getStartTime(),
                    savedSeats.stream().map(ShowSeat::getSeat).collect(Collectors.toList()),
                    request.getPhoneNumber(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to save show seats: {}", e.getMessage());
            throw new RuntimeException("Failed to create booking", e);
        }
    }

    private double calculatePrice(int numberOfSeats) {
        // Simple pricing logic - can be enhanced later
        return numberOfSeats * 200.0; // Assuming 200 per seat
    }

    private boolean processPaymentWithGateway(BookingRequest request) throws InterruptedException {
        log.info("Processing payment for request: {}", request);
        // Implement actual payment gateway integration here
        Thread.sleep(10000);
        boolean success = Math.random() > 0.1; // 90% success rate
        log.info("Payment processing result: {}", success);
        return success;
    }

    private void revertPayment(BookingRequest request) {
        try {
            log.info("Initiating payment reversal for request: {}", request);
            // Simulate payment reversal process
            Thread.sleep(5000); // Simulate API call delay
            
            // Simulate different reversal scenarios
            double random = Math.random();
            if (random > 0.05) { // 95% success rate for reversal
                log.info("Payment reversal successful for request: {}", request);
                // Here you would typically:
                // 1. Update transaction status in your system
                // 2. Send notification to user
                // 3. Update any relevant records
            } else {
                log.error("Payment reversal failed for request: {}", request);
                // Here you would typically:
                // 1. Log the failure
                // 2. Trigger manual intervention process
                // 3. Notify support team
            }
        } catch (Exception e) {
            log.error("Error during payment reversal: {}", e.getMessage());
            // Here you would typically:
            // 1. Log the error
            // 2. Trigger manual intervention process
            // 3. Notify support team
        }
    }
}
