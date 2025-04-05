package com.movie.bookMyShow.util;
import com.movie.bookMyShow.service.CityService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY_STRING = "YourSuperSecretKeyWithAtLeast32CharactersLong";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    private static final Long EXPIRATION_TIME = 1000 * 60 * 60 * 24L;

    public Long getExpirationTime(){
        return EXPIRATION_TIME;
    }

    public SecretKey getSigningKey() {
        return SECRET_KEY;
    }
    @Autowired
    private CityService cityService;
    // ✅ Generate Token
    public String generateToken(String cityName) {
        Long cityId = cityService.getIdByCity(cityName);
        if(cityId == null){
            throw new RuntimeException("City not found: " + cityName);
        }
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
}
