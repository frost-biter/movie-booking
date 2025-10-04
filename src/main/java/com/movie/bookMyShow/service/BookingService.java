package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.*;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final ShowRepo showRepo;
    private final SeatRepo seatRepo;
    private final SeatHoldService seatHoldService;
    private final PaymentService paymentService;
    private final BookingRepo bookingRepo;
    private final CryptoGatewayFactory cryptoGatewayFactory;

    public BookingService(
            ShowRepo showRepo,
            SeatRepo seatRepo,
            SeatHoldService seatHoldService,
            PaymentService paymentService,
            BookingRepo bookingRepo,
            CryptoGatewayFactory cryptoGatewayFactory) {
        this.showRepo = showRepo;
        this.seatRepo = seatRepo;
        this.seatHoldService = seatHoldService;
        this.paymentService = paymentService;
        this.bookingRepo = bookingRepo;
        this.cryptoGatewayFactory = cryptoGatewayFactory;
    }

    @Transactional
    public BookingResponse initiateBooking(BookingRequest request) {
        // 1. Validate show exists
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        // 2. Validate all seats exist
        List<Seat> seats = request.getSeatIds().stream()
                .map(seatId -> seatRepo.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId)))
                .toList();

        // 3. Check seat availability in DB only (Redis holds checked atomically in holdSeats)
        seatHoldService.areSeatsAvailable(request.getShowId(), request.getSeatIds());
        
        // 4. Create hold in Redis (this will atomically check and acquire seats)
        String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());

            // Calculate and set price
            double price = calculatePrice(request.getSeatIds(), request.getPaymentMethod());
            request.setPrice(price);
            request.setHoldId(holdId);
            // Handle crypto payments differently
            String depositAddress = null;
            if ("ETH".equals(request.getPaymentMethod())) {
                CryptoGateway cryptoGateway = cryptoGatewayFactory.getCryptoGateway(request.getPaymentMethod());
                depositAddress = cryptoGateway.generateDepositAddress(holdId);
                request.setPublicKey(depositAddress);
            }
        paymentService.processPaymentAsync(holdId, request, show, seats);
        if( depositAddress == null) {
            depositAddress = holdId; // For non-crypto payments
        }
        return new BookingResponse("Payment Process Initiated and seat held for 5mins", holdId, depositAddress, request.getPaymentMethod(), price);
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
        
        List<SeatDTO> seatDTOs = booking.getSeats().stream()
                .map(SeatDTO::fromSeat)
                .toList();
        
        return new TicketDTO(
                booking.getShow().getShowId(),
                booking.getShow().getMovie().getMovieName(),
                booking.getShow().getTheatre().getTheatreName(),
                booking.getShow().getStartTime(),
                seatDTOs,
                booking.getPhoneNumber(),
                booking.getBookingTime()
        );
    }
}
