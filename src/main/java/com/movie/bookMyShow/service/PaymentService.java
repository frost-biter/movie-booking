package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.enums.BookingStatus;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.enums.SeatStatus;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.PaymentProcessingException;
import com.movie.bookMyShow.exception.SeatHoldException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.PaymentRecord;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.PaymentRecordRepo;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {
    private static final int PAYMENT_TIMEOUT_SECONDS = 300; // 5 minutes to match seat hold duration
    private static final int MAX_PAYMENT_RETRIES = 3;
    private static final int CRYPTO_PAYMENT_TIMEOUT_SECONDS = 300; // 5 minutes for crypto

    @Autowired
    private SeatHoldService seatHoldService;
    @Autowired
    private ShowSeatRepo showSeatRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;
    @Autowired
    private KafkaTemplate<String, TicketDTO> kafkaBookMovieTemplate;
    @Autowired
    private PaymentRecordRepo paymentRecordRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPaymentAsync(String holdId, BookingRequest request, Show show, List<Seat> seats) {
        PaymentRecord paymentRecord = createInitialPaymentRecord(holdId, request);
        
        try {
            // Validate hold first
            if (!seatHoldService.validateHold(request.getShowId(), holdId, request.getSeatIds())) {
                throw new SeatHoldException("Hold validation failed for holdId: " + holdId);
            }

            // Process payment with retry logic
            processPaymentWithRetry(holdId, request, paymentRecord, show, seats);
        } catch (Exception e) {
            handleFailure(holdId, request, paymentRecord, show, seats, 
                         "Initial validation failed: " + e.getMessage(), false);
        }
    }

    private void processPaymentWithRetry(String holdId, BookingRequest request, PaymentRecord paymentRecord, 
                                       Show show, List<Seat> seats) {
        PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(request.getPaymentMethod());
        final int maxRetries = MAX_PAYMENT_RETRIES;
        final AtomicInteger retryCount = new AtomicInteger(0);
        
        // Use longer timeout for crypto payments
        final int timeoutSeconds = request.getPaymentMethod().startsWith("ETH") ? 
            CRYPTO_PAYMENT_TIMEOUT_SECONDS : PAYMENT_TIMEOUT_SECONDS;
        
        CompletableFuture.runAsync(() -> {
            while (retryCount.get() < maxRetries) {
                try {
                    CompletableFuture<Boolean> paymentFuture = gateway.processPayment(request)
                        .orTimeout(timeoutSeconds, TimeUnit.SECONDS);

                    paymentFuture.thenAccept(paymentSuccess -> {
                        try {
                            log.info("Payment result received for holdId: {}, success: {}", holdId, paymentSuccess);
                            updatePaymentRecord(paymentRecord, paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED, 
                                              paymentSuccess ? null : "Payment processing failed");

                            if (paymentSuccess) {
                                handleSuccessfulPayment(holdId, request, paymentRecord, show, seats);
                            } else {
                                handleFailure(holdId, request, paymentRecord, show, seats, 
                                            "Payment processing failed", false);
                            }
                        } catch (Exception e) {
                            handleFailure(holdId, request, paymentRecord, show, seats, 
                                        "Payment result handling failed: " + e.getMessage(), true);
                        }
                    }).exceptionally(ex -> {
                        if (ex instanceof java.util.concurrent.TimeoutException) {
                            log.warn("Payment timeout for holdId: {}, retry count: {}", holdId, retryCount.get());
                            if (retryCount.get() < maxRetries - 1) {
                                retryCount.incrementAndGet();
                                processPaymentWithRetry(holdId, request, paymentRecord, show, seats);
                            } else {
                                handleFailure(holdId, request, paymentRecord, show, seats, 
                                            "Payment timeout after " + maxRetries + " retries", true);
                            }
                        } else {
                            handleFailure(holdId, request, paymentRecord, show, seats, 
                                        "Payment processing error: " + ex.getMessage(), true);
                        }
                        return null;
                    });
                    break; // Exit retry loop if payment initiated successfully
                } catch (Exception e) {
                    retryCount.incrementAndGet();
                    if (retryCount.get() >= maxRetries) {
                        handleFailure(holdId, request, paymentRecord, show, seats, 
                                    "Payment processing failed after " + maxRetries + " retries: " + e.getMessage(), 
                                    true);
                        break;
                    }
                    log.warn("Payment attempt {} failed for holdId: {}, retrying...", retryCount.get(), holdId);
                }
            }
        });
    }

    private void handleSuccessfulPayment(String holdId, BookingRequest request, PaymentRecord paymentRecord, 
                                       Show show, List<Seat> seats) {
        try {
            log.info("Creating booking for holdId: {}, showId: {}, seats: {}", 
                    holdId, request.getShowId(), request.getSeatIds());
            TicketDTO ticket = createBookingAndGetTicket(request, holdId, show, seats);
            log.info("Ticket generated successfully for holdId: {}, ticket details: {}", holdId, ticket);
            
            sendTicketNotification(holdId, ticket);
        } catch (Exception e) {
            handleFailure(holdId, request, paymentRecord, show, seats, 
                        "Booking creation failed: " + e.getMessage(), true);
        }
    }

    private void handleFailure(String holdId, BookingRequest request, PaymentRecord paymentRecord, 
                             Show show, List<Seat> seats, String errorMessage, boolean shouldRevertPayment) {
        log.error("Handling failure for holdId: {}, error: {}", holdId, errorMessage);
        
        try {
            // Create failed booking record
            Booking failedBooking = new Booking();
            failedBooking.setHoldId(holdId);
            failedBooking.setShow(show);
            failedBooking.setSeats(seats);
            failedBooking.setPrice(request.getPrice());
            failedBooking.setPhoneNumber(request.getPhoneNumber());
            failedBooking.setBookingTime(LocalDateTime.now());
            failedBooking.setStatus(BookingStatus.FAILED);

            bookingRepo.save(failedBooking);
            log.info("Created failed booking record for holdId: {}", holdId);

            // Release hold immediately since this transaction is already failed
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
            log.info("Released hold for holdId: {}", holdId);

            // Revert payment if needed
            if (shouldRevertPayment) {
                log.info("Initiating payment reversal for holdId: {}", holdId);
                revertPayment(request)
                    .thenAccept(reversalSuccess -> {
                        PaymentStatus status = reversalSuccess ? PaymentStatus.REVERTED : PaymentStatus.FAILED;
                        String finalErrorMessage = reversalSuccess ? 
                            "Payment successfully reverted" : 
                            errorMessage + " (Payment reversal failed)";
                        updatePaymentRecord(paymentRecord, status, finalErrorMessage);
                    });
            } else {
                updatePaymentRecord(paymentRecord, PaymentStatus.FAILED, errorMessage);
            }
        } catch (Exception e) {
            log.error("Error in failure handling for holdId: {}: {}", holdId, e.getMessage(), e);
            updatePaymentRecord(paymentRecord, PaymentStatus.FAILED, 
                              errorMessage + " (Error in failure handling: " + e.getMessage() + ")");
            // Release hold in case of error
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());
        }
    }

    private CompletableFuture<Boolean> revertPayment(BookingRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Initiating payment reversal for request: {}", request);
                PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(request.getPaymentMethod());
                boolean reversalSuccess = gateway.revertPayment(request);
                log.info("Payment reversal completed for holdId: {}, success: {}", request.getHoldId(), reversalSuccess);
                return reversalSuccess;
            } catch (Exception e) {
                log.error("Error during payment reversal: {}", e.getMessage());
                return false;
            }
        });
    }

    private PaymentRecord createInitialPaymentRecord(String holdId, BookingRequest request) {
        PaymentRecord record = PaymentRecord.builder()
                .holdId(holdId)
                .showId(request.getShowId())
                .paymentMethod(request.getPaymentMethod())
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getPrice())
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
    private TicketDTO createBookingAndGetTicket(BookingRequest request, String holdId, Show show, List<Seat> seats) {
        log.info("Starting booking creation process for holdId: {}", holdId);

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
                    return createBookingAndGetTicket(request, newHoldId, show, seats);
                }
            }
            
            log.warn("Seats are no longer available, reverting payment");
            revertPayment(request);
            throw new SeatHoldException("Seats are no longer available");
        }

        log.info("Creating show seats for holdId: {}", holdId);
        List<ShowSeat> showSeats = seats.stream()
                .map(seat -> ShowSeat.builder()
                        .show(show)
                        .seat(seat)
                        .status(SeatStatus.BOOKED)
                        .build())
                .collect(Collectors.toList());

        try {
            // Save show seats first
            List<ShowSeat> savedSeats = showSeatRepo.saveAll(showSeats);
            log.info("Successfully saved {} show seats for holdId: {}", savedSeats.size(), holdId);

            // Create and save booking
            Booking booking = new Booking();
            booking.setHoldId(holdId);
            booking.setShow(show);
            booking.setSeats(seats);
            booking.setPrice(request.getPrice());
            booking.setPhoneNumber(request.getPhoneNumber());
            booking.setBookingTime(LocalDateTime.now());
            booking.setStatus(BookingStatus.CONFIRMED);

            Booking savedBooking = bookingRepo.save(booking);
            log.info("Successfully created booking for holdId: {}", holdId);

            // Release the hold since booking is successful
            seatHoldService.releaseHold(request.getShowId(), holdId, request.getSeatIds());

            return new TicketDTO(
                    show.getShowId(),
                    show.getMovie().getMovieName(),
                    show.getTheatre().getTheatreName(),
                    show.getStartTime(),
                    seats,
                    request.getPhoneNumber(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to save show seats or booking for holdId: {}: {}", holdId, e.getMessage(), e);
            throw new PaymentProcessingException("Failed to create booking", e);
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
}
