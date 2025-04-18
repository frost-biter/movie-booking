package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private SeatHoldService seatHoldService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private BookingRepo bookingRepo;

    @Transactional
    public ApiResponse initiateBooking(BookingRequest request) {
        // 1. Validate show exists
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // 2. Check seat availability in Redis and DB
        if (seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds())) {
            // 3. Create hold in Redis
            String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());
            
            // 4. Initiate async payment
            paymentService.processPaymentAsync(holdId, request);
            
            // 5. Return response immediately
            return new ApiResponse(202, "Payment process initiated. Seats held for 10 minutes. Hold ID: " + holdId);
        } else {
            throw new SeatAlreadyHeldException("One or more seats are already held or booked");
        }
    }

    public TicketDTO getBooking(String holdId) {
        Booking booking = bookingRepo.findByHoldId(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for hold ID: " + holdId));
        
        return new TicketDTO(
                booking.getShow().getShowId(),
                booking.getShow().getMovie().getMovieName(),
                booking.getShow().getTheatre().getTheatreName(),
                booking.getShow().getStartTime(),
                booking.getSeats(),
                booking.getPhoneNumber(),
                booking.getBookingTime()
        );
    }
}
