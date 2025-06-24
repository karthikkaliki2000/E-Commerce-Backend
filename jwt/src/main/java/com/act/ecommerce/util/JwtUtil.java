package com.act.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
    private static final SecretKey SIGNING_KEY = Jwts.SIG.HS256.key().build(); // Secure key generation

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SIGNING_KEY)
                .compact();
    }

    public String getUserNameFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token, String username) {
        return username.equals(getUserNameFromToken(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaimsFromToken(token, Claims::getExpiration).before(new Date());
    }

    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
