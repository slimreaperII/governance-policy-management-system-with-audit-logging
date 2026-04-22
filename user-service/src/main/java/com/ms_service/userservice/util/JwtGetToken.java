package com.ms_service.userservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtGetToken {
    private final static Key SECRET = Keys.hmacShaKeyFor("EEf4D7OfNX3XB9Z0/UzLguqjQvnmGMCIXkCCpLi0Z2I=".getBytes());
    private final static long EXPIRATION = 1000 * 60 * 60 * 24;
    public static String generateToken(String username, String role) {
        return Jwts.builder()
                    .subject(username)
                    .claim("role", role)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                    .signWith(SECRET)
                    .compact();
    }
}
