package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.dto.TicketDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyHeldException;
import com.movie.bookMyShow.model.Booking;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.repo.BookingRepo;
import com.movie.bookMyShow.repo.SeatRepo;
import com.movie.bookMyShow.repo.ShowRepo;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGateway;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGatewayFactory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private SeatRepo seatRepo;
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

        // 2. Validate all seats exist
        List<Seat> seats = request.getSeatIds().stream()
                .map(seatId -> seatRepo.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId)))
                .toList();

        // 3. Check seat availability in Redis and DB
        if (seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds())) {
            // 4. Create hold in Redis
            String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());

            // Calculate and set price
            double price = calculatePrice(request.getSeatIds(), request.getPaymentMethod());
            request.setPrice(price);

            // Handle crypto payments differently
            if ("ETH".equals(request.getPaymentMethod())) {
                CryptoGateway cryptoGateway = cryptoGatewayFactory.getCryptoGateway(request.getPaymentMethod());
                String depositAddress = cryptoGateway.generateDepositAddress(holdId);
                request.setHoldId(holdId);
                request.setPublicKey(depositAddress);
                // Start async payment monitoring in background
                paymentService.processPaymentAsync(holdId, request, show, seats);
                
                // Return deposit address to user
                return new ApiResponse(1, "Send payment to this ETH address: " + depositAddress + 
                    ". Hold ID: " + holdId + 
                    ". Required amount: " + price + " ETH");
            }

            // For non-crypto payments, proceed with normal flow
            paymentService.processPaymentAsync(holdId, request, show, seats);
            return new ApiResponse(202, "Payment process initiated. Seats held for 5 minutes. Hold ID: " + holdId);
        } else {
            throw new SeatAlreadyHeldException("Seats are not available");
        }
    }

    private double calculatePrice(List<Long> seatIds, String paymentMethod) {
        // Simple pricing logic - can be enhanced later
        if("ETH".equals(paymentMethod)) {
            return seatIds.size() * 0.001; // Assuming 0.01 ETH per seat
        }
        return seatIds.size() * 200.0; // Assuming 200 per seat
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
