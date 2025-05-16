package com.movie.bookMyShow.service.payment.Crypto;

import com.movie.bookMyShow.enums.PaymentStatus;

public interface CryptoGateway {
    String generateDepositAddress(String holdId);
    PaymentStatus checkPaymentStatus(String address, double requiredAmount);
}
