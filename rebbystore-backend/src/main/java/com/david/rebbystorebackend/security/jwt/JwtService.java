package com.david.rebbystorebackend.security.jwt;

import com.david.rebbystorebackend.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        if (jwtProperties.getSecret() == null
                || jwtProperties.getSecret().isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }

        if (jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes long"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getExpirationMs());

        List<String> roles = principal.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("email", principal.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = parseToken(token).get("userId");

        if (userId instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("JWT userId claim is missing or invalid");
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}