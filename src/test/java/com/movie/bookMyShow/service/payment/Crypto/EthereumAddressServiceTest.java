package com.movie.bookMyShow.service.payment.Crypto;

import com.movie.bookMyShow.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class EthereumAddressServiceTest {

    @Autowired
    private EthereumAddressService ethereumAddressService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ethereumAddressService, "xpub", "test-xpub");
    }

    @Test
    void testGenerateNewAddress() {
        String address = ethereumAddressService.generateNewAddress("test-user");
        assertNotNull(address);
        assertTrue(address.startsWith("0x"));
        assertEquals(42, address.length()); // Ethereum addresses are 42 characters (0x + 40 hex chars)
    }

    @Test
    void testGenerateNewAddressWithDifferentUsers() {
        String address1 = ethereumAddressService.generateNewAddress("user1");
        String address2 = ethereumAddressService.generateNewAddress("user2");
        
        assertNotNull(address1);
        assertNotNull(address2);
        assertNotEquals(address1, address2); // Different users should get different addresses
    }

    @Test
    void testGenerateNewAddressWithSameUser() {
        String address1 = ethereumAddressService.generateNewAddress("user1");
        String address2 = ethereumAddressService.generateNewAddress("user1");
        
        assertNotNull(address1);
        assertNotNull(address2);
        assertEquals(address1, address2); // Same user should get the same address
    }
} 