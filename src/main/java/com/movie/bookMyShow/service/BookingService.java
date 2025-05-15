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
import com.movie.bookMyShow.service.payment.Crypto.CryptoGateway;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGatewayFactory;
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
    @Autowired
    private CryptoGatewayFactory cryptoGatewayFactory;
    @Transactional
    public ApiResponse initiateBooking(BookingRequest request) {
        // 1. Validate show exists
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // 2. Check seat availability in Redis and DB
        if (seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds())) {
            // 3. Create hold in Redis
            String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());

            // Handle crypto payments differently
            if ("ETH".equals(request.getPaymentMethod())) {
                CryptoGateway cryptoGateway = cryptoGatewayFactory.getCryptoGateway(request.getPaymentMethod());
                String depositAddress = cryptoGateway.generateDepositAddress(holdId);
                request.setHoldId(holdId);
                request.setPublicKey(depositAddress);
                // Start async payment monitoring in background
                paymentService.processPaymentAsync(holdId, request);
                
                // Return deposit address to user
                return new ApiResponse(1, "Send payment to this ETH address: " + depositAddress+". Hold ID: " + holdId);
            }

            // For non-crypto payments, proceed with normal flow
            paymentService.processPaymentAsync(holdId, request);
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
