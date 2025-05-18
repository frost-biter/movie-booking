package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGateway;
import com.movie.bookMyShow.service.payment.Crypto.CryptoGatewayFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private CryptoGatewayFactory cryptoGatewayFactory;

    @GetMapping("/status/{paymentMethod}/{address}")
    public ApiResponse checkPaymentStatus(
            @PathVariable String paymentMethod,
            @PathVariable String address,
            @RequestParam(required = false) Double requiredAmount) {
        try {
            CryptoGateway gateway = cryptoGatewayFactory.getCryptoGateway(paymentMethod);
            PaymentStatus status = gateway.checkPaymentStatus(address,requiredAmount);
            
            return new ApiResponse(200, "Payment status: " + status.name());
        } catch (Exception e) {
            return new ApiResponse(500, "Error checking payment status: " + e.getMessage());
        }
    }
} 