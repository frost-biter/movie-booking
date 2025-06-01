package com.movie.bookMyShow.controller;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.service.payment.PaymentGateway;
import com.movie.bookMyShow.service.payment.PaymentGatewayFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    @GetMapping("/status/{paymentMethod}/{address}")
    public ApiResponse checkPaymentStatus(
            @PathVariable String paymentMethod,
            @PathVariable String address,
            @RequestParam(required = false) Double requiredAmount) {

        PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(paymentMethod);
        try {
            PaymentStatus status = gateway.checkPaymentStatus(address,requiredAmount);
            if(status == PaymentStatus.FAILED || status == PaymentStatus.INVALID_AMOUNT) {
                return new ApiResponse(400, "Payment failed or insufficient amount.");
            }
            if(status == PaymentStatus.PENDING) {
                return new ApiResponse(202, "Payment is still pending.");
            }
            return new ApiResponse(200, "Payment status: " + status.name());
        } catch (Exception e) {
            return new ApiResponse(500, "Error checking payment status: " + e.getMessage());
        }
    }
} 