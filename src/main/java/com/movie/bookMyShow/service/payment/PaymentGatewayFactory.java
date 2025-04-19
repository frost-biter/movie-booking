package com.movie.bookMyShow.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.movie.bookMyShow.exception.ResourceNotFoundException;

import java.util.Map;

@Service
public class PaymentGatewayFactory {
    
    @Autowired
    private Map<String, PaymentGateway> paymentGateways;

    public PaymentGateway getPaymentGateway(String paymentMethod) {

        String gatewayName = paymentMethod + "PaymentGateway";
        PaymentGateway gateway = paymentGateways.get(gatewayName);
        if (gateway == null) {
            throw new ResourceNotFoundException("No payment gateway found for method: " + paymentMethod);
        }
        return gateway;
    }
} 