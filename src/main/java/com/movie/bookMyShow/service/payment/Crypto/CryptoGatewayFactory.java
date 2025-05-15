package com.movie.bookMyShow.service.payment.Crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CryptoGatewayFactory {

    private final Map<String, CryptoGateway> cryptoGateways;

    @Autowired
    public CryptoGatewayFactory(Map<String, CryptoGateway> cryptoGateways) {
        this.cryptoGateways = cryptoGateways;
    }

    public CryptoGateway getCryptoGateway(String crypto) {
        String cryptoGatewayName = crypto + "PaymentGateway";
        return cryptoGateways.get(cryptoGatewayName);
    }
}
