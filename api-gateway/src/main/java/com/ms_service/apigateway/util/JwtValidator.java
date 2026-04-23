package com.ms_service.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtValidator {

    private static final SecretKey SECRET = Keys.hmacShaKeyFor("EEf4D7OfNX3XB9Z0/UzLguqjQvnmGMCIXkCCpLi0Z2I=".getBytes());

    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validate(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public static String extractRole(String token) {
        String role = extractClaims(token).get("role", String.class);
        return role.toUpperCase();
    }
}