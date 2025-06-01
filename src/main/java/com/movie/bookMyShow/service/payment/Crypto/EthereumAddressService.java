package com.movie.bookMyShow.service.payment.Crypto;

import com.movie.bookMyShow.util.AddressUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class EthereumAddressService implements CryptoAddressService {

    @Value("${crypto.eth.xpub}")
    private String xpub;

    public String generateNewAddress(String holdId) {
        System.out.print("\nGetting instance for hold ID: " + holdId+"\n xpub : "+ xpub +"\n");
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(holdId.getBytes());
        // Use the first 4 bytes of the hash to create a positive int index
//            int index = ((hash[0] & 0xFF) << 24) | ((hash[1] & 0xFF) << 16) | ((hash[2] & 0xFF) << 8) | (hash[3] & 0xFF);
        int index = Math.abs(holdId.hashCode());

        return AddressUtil.deriveEthAddressFromXpub(xpub, index);
    }
}
