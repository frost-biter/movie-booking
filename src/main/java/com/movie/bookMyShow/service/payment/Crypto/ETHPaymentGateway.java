package com.movie.bookMyShow.service.payment.Crypto;

import com.movie.bookMyShow.dto.BookingRequest;
import com.movie.bookMyShow.enums.PaymentStatus;
import com.movie.bookMyShow.service.payment.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class ETHPaymentGateway implements PaymentGateway, CryptoGateway {

    @Autowired
    private EthereumAddressService ethereumAddressService;

    @Value("${ethereum.node.url}")
    private String ethereumNodeUrl;

    @Value("${ethereum.payment.confirmation.blocks}")
    private int requiredConfirmations;

    @Value("${ethereum.transaction.scan.interval.seconds}")
    private int scanInterval;

    @Value("${ethereum.payment.timeout.minutes}")
    private int paymentTimeoutMinutes;

    private Web3j web3j;

    @PostConstruct
    public void init() {
        this.web3j = Web3j.build(new HttpService(ethereumNodeUrl));
    }

    @Override
    public String generateDepositAddress(String holdId) {
        return ethereumAddressService.generateNewAddress(holdId);
    }

    @Override
    @Async
    public CompletableFuture<Boolean> processPayment(BookingRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String depositAddress = request.getPublicKey();
                double requiredAmount = request.getPrice();
                log.info("Starting payment monitoring for address {} and hold {} with required amount: {} ETH", 
                    depositAddress, request.getHoldId(), requiredAmount);
                
                Instant startTime = Instant.now();
                Duration timeout = Duration.ofMinutes(paymentTimeoutMinutes);
                int currentDelay = scanInterval;
                final int maxDelay = 300;
                final double backoffFactor = 2;

                while (Duration.between(startTime, Instant.now()).compareTo(timeout) < 0) {
                    Thread.sleep(currentDelay * 1000L);
                    log.info("[{}] Checking payment status in processPayment for address: {} (Delay: {}s)", 
                        LocalDateTime.now(), depositAddress, currentDelay);
                    PaymentStatus status = checkPaymentStatus(depositAddress, requiredAmount);
                    log.info("[{}] Payment status in processPayment for address {}: {}", 
                        LocalDateTime.now(), depositAddress, status);
                    
                    if (status == PaymentStatus.SUCCESS) {
                        log.info("[{}] Payment detected for address {} and hold {} with correct amount", 
                            LocalDateTime.now(), depositAddress, request.getHoldId());
                        return true;
                    }

                    currentDelay = (int) Math.min(currentDelay * backoffFactor, maxDelay);
                    log.info("[{}] Next payment check in {} seconds for hold {} (New delay: {}s)", 
                        LocalDateTime.now(), currentDelay, request.getHoldId(), currentDelay);
                }

                log.error("[{}] Payment timeout for hold {}", LocalDateTime.now(), request.getHoldId());
                return false;
            } catch (Exception e) {
                log.error("[{}] Error in payment monitoring for hold {}: {}", 
                    LocalDateTime.now(), request.getHoldId(), e.getMessage(), e);
                return false;
            }
        });
    }

    private BigInteger getBalance(String address) throws Exception {
        log.info("Checking balance for address: {}", address);
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        if (balance.hasError()) {
            log.error("Error getting balance for address {}: {}", address, balance.getError().getMessage());
            throw new Exception("Error getting balance: " + balance.getError().getMessage());
        }
        BigInteger balanceValue = balance.getBalance();
        log.info("Balance for address {}: {} wei", address, balanceValue);
        return balanceValue;
    }

    @Override
    public boolean revertPayment(BookingRequest request) {
        log.info("Payment reversion not supported for ETH payments");
        return false;
    }

    @Override
    public PaymentStatus checkPaymentStatus(String address, double requiredAmount) {
        try {
            log.info("Checking payment status for address: {} with required amount: {} ETH", address, requiredAmount);
            
            BigInteger currentBalance = getBalance(address);
            
            if (currentBalance.compareTo(BigInteger.ZERO) <= 0) {
                return PaymentStatus.PENDING;
            }
            
            // Convert required amount to wei (1 ETH = 10^18 wei)
            BigDecimal requiredAmountDecimal = BigDecimal.valueOf(requiredAmount);
            BigInteger requiredAmountWei = requiredAmountDecimal.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            
            // Check if the received amount matches the required amount (with 1% tolerance)
            BigDecimal tolerance = requiredAmountDecimal.multiply(BigDecimal.valueOf(0.01));
            BigInteger toleranceWei = tolerance.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
            
            if (currentBalance.compareTo(requiredAmountWei.subtract(toleranceWei)) >= 0 && 
                currentBalance.compareTo(requiredAmountWei.add(toleranceWei)) <= 0) {
                
                // Additional safety check: verify block confirmations
//                if (!hasEnoughConfirmations()) {
//                    log.info("Amount matches but waiting for block confirmations");
//                    return PaymentStatus.PENDING;
//                }
                
                return PaymentStatus.SUCCESS;
            }
            
            return PaymentStatus.INVALID_AMOUNT;
        } catch (Exception e) {
            log.error("Error checking payment status for address {}: {}", address, e.getMessage());
            return PaymentStatus.FAILED;
        }
    }
}

