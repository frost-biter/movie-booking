package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.*;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.exception.SeatAlreadyBookedException;
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
import java.util.stream.Collectors;

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
        // 1. Validate show and seats exist.
        Show show = showRepo.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.getShowId()));

        List<Seat> seats = seatRepo.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more seats not found.");
        }

        try {
            // 2. Atomically acquire a hold on the seats.
            // This single method call now handles checking DB availability AND acquiring Redis holds safely.
            String holdId = seatHoldService.holdSeats(request.getShowId(), request.getSeatIds());
            
            // 3. If the hold is successful, proceed with payment logic.
            double price = calculatePrice(seats, request.getPaymentMethod());
            request.setPrice(price);
            request.setHoldId(holdId);

            String depositAddress = null;
            if ("ETH".equals(request.getPaymentMethod())) {
                CryptoGateway cryptoGateway = cryptoGatewayFactory.getCryptoGateway(request.getPaymentMethod());
                depositAddress = cryptoGateway.generateDepositAddress(holdId);
                request.setPublicKey(depositAddress);
            }

            // 4. Asynchronously process the payment.
            paymentService.processPaymentAsync(holdId, request, show, seats);

            // For non-crypto payments, the holdId can act as the transaction reference.
            String paymentReference = (depositAddress != null) ? depositAddress : holdId;
            
            return new BookingResponse("Payment process initiated. Seats held for 5 minutes.", holdId, paymentReference, request.getPaymentMethod(), price);

        } catch (SeatAlreadyBookedException | SeatAlreadyHeldException e) {
            // These exceptions are expected and should be re-thrown to be handled by the GlobalExceptionHandler.
            throw e;
        }
    }

    private double calculatePrice(List<Seat> seats, String paymentMethod) {
        // This can be enhanced later with pricing based on SeatCategory
        if ("ETH".equals(paymentMethod)) {
            return seats.size() * 0.001; // Example: 0.001 ETH per seat
        }
        return seats.size() * 200.0; // Example: 200 INR per seat
    }

    public TicketDTO getBooking(String holdId) {
        Booking booking = bookingRepo.findByHoldId(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for hold ID: " + holdId));
        
        List<SeatDTO> seatDTOs = booking.getSeats().stream()
                .map(SeatDTO::fromSeat)
                .collect(Collectors.toList());
        
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

