package com.relatosdepapel.users.service;

import com.relatosdepapel.users.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "relatosdepapeljwtsecretkey2026relatosdepapel";

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(Integer id, String email, String role) {

        Date now = new Date();

        Date expiration = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims obtainClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}