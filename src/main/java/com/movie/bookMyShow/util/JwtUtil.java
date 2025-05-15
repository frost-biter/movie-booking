package com.movie.bookMyShow.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY_STRING = "YourSuperSecretKeyWithAtLeast32CharactersLong";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    private static final Long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30L;

    public Long getExpirationTime(){
        return EXPIRATION_TIME;
    }

    public SecretKey getSigningKey() {
        return SECRET_KEY;
    }
    // ✅ Generate Token
    public String generateToken(Long cityId) {

        return Jwts.builder()
                .subject(String.valueOf(cityId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getExpirationTime()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Integer extractCityId(String token) {
        System.out.println("🔍 Extracting cityId from token: [" + token + "]");
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        System.out.println("🔍 cityId from token: [" + claims.getSubject() + "]");
        return Integer.parseInt(claims.getSubject()); // Convert back to int
    }

    public String generateAdminToken(String username, String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getExpirationTime()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // ✅ Latest way to verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload(); // ✅ No .getBody()
    }

    public String extractAdminUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean validateToken(String token, String expectedUsername) {
        try {
            String username = extractAdminUsername(token);
            return username.equals(expectedUsername);
        } catch (SecurityException | IllegalArgumentException e) {
            return false;
        }
    }
}
