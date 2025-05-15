package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.PaymentRecord;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.PaymentRecordRepo;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.repo.ShowSeatRepo;
import com.movie.bookMyShow.service.payment.PaymentGateway;
import com.movie.bookMyShow.service.payment.PaymentGatewayFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    private PaymentGatewayFactory paymentGatewayFactory;
    @Autowired
    private KafkaTemplate<String , TicketDTO> kafkaBookMovieTemplate;
    @Autowired
    private PaymentRecordRepo paymentRecordRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPaymentAsync(String holdId, BookingRequest request) {
        PaymentRecord paymentRecord = createInitialPaymentRecord(holdId, request);
        
        try {
            validateAndProcessPayment(holdId, request, paymentRecord);
        } catch (Exception e) {
            handlePaymentFailure(holdId, request, paymentRecord, e);
        }
    }

    private void validateAndProcessPayment(String holdId, BookingRequest request, PaymentRecord paymentRecord) {
        if (!seatHoldService.validateHold(request.getShowId(), holdId, request.getSeatIds())) {
            throw new IllegalStateException("Hold validation failed for holdId: " + holdId);
        }

        try {
            PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(request.getPaymentMethod());
            gateway.processPayment(request)
                .thenAccept(paymentSuccess -> handlePaymentResult(holdId, request, paymentRecord, paymentSuccess))
                .exceptionally(ex -> {
                    handlePaymentFailure(holdId, request, paymentRecord, ex);
                    return null;
                });
        } catch (Exception e) {
            handlePaymentFailure(holdId, request, paymentRecord, e);
        }
    }

    private void handlePaymentResult(String holdId, BookingRequest request, PaymentRecord paymentRecord, boolean paymentSuccess) {
        try {
            log.info("Payment result received for holdId: {}, success: {}", holdId, paymentSuccess);
            updatePaymentRecord(paymentRecord, paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, 
                              paymentSuccess ? null : "Payment processing failed");

            if (paymentSuccess) {
                log.info("Initiating successful payment handling for holdId: {}", holdId);
                handleSuccessfulPayment(holdId, request, paymentRecord);
            } else {
                handleFailedPayment(holdId, request);
            }
        } catch (Exception e) {
            log.error("Error in payment result handling for holdId: {}: {}", holdId, e.getMessage(), e);
            handlePaymentFailure(holdId, request, paymentRecord, e);
        }
    }

    private void handleSuccessfulPayment(String holdId, BookingRequest request, PaymentRecord paymentRecord) {
        try {
            log.info("Creating booking for holdId: {}, showId: {}, seats: {}", 
                    holdId, request.getShowId(), request.getSeatIds());
            TicketDTO ticket = createBookingAndGetTicket(request, holdId);
            log.info("Ticket generated successfully for holdId: {}, ticket details: {}", holdId, ticket);
            
            sendTicketNotification(holdId, ticket);
        } catch (Exception e) {
            log.error("Failed to create booking for holdId: {}: {}", holdId, e.getMessage(), e);
            handleBookingFailure(holdId, request, paymentRecord, e);
        }
    }

    private void sendTicketNotification(String holdId, TicketDTO ticket) {
        try {
            kafkaBookMovieTemplate.send("book_movie", holdId, ticket)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send ticket notification for holdId: {}: {}", 
                                 holdId, ex.getMessage());
                    } else {
                        log.info("Successfully sent ticket notification for holdId: {}", holdId);
                    }
                });
        } catch (Exception e) {
            log.error("Error sending ticket notification for holdId: {}: {}", 
                     holdId, e.getMessage());
            // Don't fail the booking if notification fails
        }
    }

    private void handleFailedPayment(String holdId, BookingRequest request) {
        log.info("Payment failed, releasing hold: {}", holdId);
        seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
    }

    private void handleBookingFailure(String holdId, BookingRequest request, PaymentRecord paymentRecord, Exception e) {
        log.error("Failed to create booking: {}", e.getMessage());
        seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
        updatePaymentRecord(paymentRecord, PaymentStatus.FAILED, "Booking creation failed: " + e.getMessage());
    }

    private void handlePaymentFailure(String holdId, BookingRequest request, PaymentRecord paymentRecord, Throwable e) {
        log.error("Error in payment processing: {}", e.getMessage());
        seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
        updatePaymentRecord(paymentRecord, PaymentStatus.FAILED, "Payment processing error: " + e.getMessage());
    }

    private PaymentRecord createInitialPaymentRecord(String holdId, BookingRequest request) {
        PaymentRecord record = PaymentRecord.builder()
                .holdId(holdId)
                .showId(request.getShowId())
                .paymentMethod(request.getPaymentMethod())
                .phoneNumber(request.getPhoneNumber())
                .amount(calculatePrice(request.getSeatIds().size()))
                .status(PaymentStatus.PENDING)
                .attemptTime(LocalDateTime.now())
                .build();
        return paymentRecordRepo.save(record);
    }

    private void updatePaymentRecord(PaymentRecord record, PaymentStatus status, String errorMessage) {
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setCompletionTime(LocalDateTime.now());
        paymentRecordRepo.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private TicketDTO createBookingAndGetTicket(BookingRequest request, String holdId) {
        log.info("Starting booking creation process for holdId: {}", holdId);
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // Validate the hold is still valid
        if (!seatHoldService.validateHold(request.getShowId(), holdId, request.getSeatIds())) {
            log.warn("Hold {} has expired or is invalid", holdId);
            
            // Check if seats are still available
            if (seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds())) {
                log.info("Seats are still available, attempting to rebook with new hold");
                // Try to get a new hold
                String newHoldId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());
                if (newHoldId != null) {
                    log.info("Successfully acquired new hold {}, proceeding with booking", newHoldId);
                    // Create booking with new hold
                    return createBookingAndGetTicket(request, newHoldId);
                }
            }
            
            log.warn("Seats are no longer available, reverting payment");
            revertPayment(request);
            throw new IllegalStateException("Seats are no longer available");
        }

        log.info("Creating show seats for holdId: {}", holdId);
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
            log.info("Successfully saved {} show seats for holdId: {}", savedSeats.size(), holdId);

            // Create and save booking
            Booking booking = new Booking();
            booking.setHoldId(holdId);
            booking.setShow(show);
            booking.setSeats(savedSeats.stream().map(ShowSeat::getSeat).collect(Collectors.toList()));
            booking.setPrice(calculatePrice(savedSeats.size()));
            booking.setPhoneNumber(request.getPhoneNumber());
            booking.setBookingTime(LocalDateTime.now());

            Booking savedBooking = bookingRepo.save(booking);
            log.info("Successfully created booking for holdId: {}", holdId);

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
            log.error("Failed to save show seats or booking for holdId: {}: {}", holdId, e.getMessage(), e);
            throw new RuntimeException("Failed to create booking", e);
        }
    }

    private double calculatePrice(int numberOfSeats) {
        // Simple pricing logic - can be enhanced later
        return numberOfSeats * 200.0; // Assuming 200 per seat
    }

    private void revertPayment(BookingRequest request) {
        try {
            log.info("Initiating payment reversal for request: {}", request);
            PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(request.getPaymentMethod());
            gateway.revertPayment(request);
        } catch (Exception e) {
            log.error("Error during payment reversal: {}", e.getMessage());
        }
    }
}
