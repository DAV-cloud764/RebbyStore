package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.security.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class GuestCheckoutTokenService {

    private static final String PURPOSE = "guest-order-confirmation";
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);

    private final SecretKey secretKey;

    public GuestCheckoutTokenService(JwtProperties properties) {
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be configured and at least 32 bytes long"
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createToken(Long orderId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(orderId.toString())
                .claim("purpose", PURPOSE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_LIFETIME)))
                .signWith(secretKey)
                .compact();
    }

    public Long requireOrderId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!PURPOSE.equals(claims.get("purpose", String.class))) {
                throw new IllegalArgumentException("Wrong token purpose");
            }
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                throw new IllegalArgumentException("Missing order id");
            }
            return Long.valueOf(claims.getSubject());
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Order confirmation token is invalid or expired"
            );
        }
    }
}
