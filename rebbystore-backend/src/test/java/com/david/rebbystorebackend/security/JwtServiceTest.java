package com.david.rebbystorebackend.security;

import com.david.rebbystorebackend.domain.entity.Role;
import com.david.rebbystorebackend.domain.entity.User;
import com.david.rebbystorebackend.security.jwt.JwtProperties;
import com.david.rebbystorebackend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();

        properties.setSecret(
                "01234567890123456789012345678901"
        );

        properties.setExpirationMs(3_600_000);

        jwtService = new JwtService(properties);

        principal = UserPrincipal.createForTesting(
                1L,
                "admin",
                "admin@rebbystore.co.tz",
                "$2a$10$dummy",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void shouldExtractUserIdWhenPresent() {
        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractUserId(token))
                .isEqualTo(1L);
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractUsername(token))
                .isEqualTo("admin");
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtService.generateToken(principal);

        assertThat(jwtService.isTokenValid(token))
                .isTrue();
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(principal);

        String[] parts = token.split("\\.");

        String signature = parts[2];

        char original = signature.charAt(0);
        char replacement = original == 'A' ? 'B' : 'A';

        String tamperedSignature =
                replacement + signature.substring(1);

        String tamperedToken =
                parts[0] + "." + parts[1] + "." + tamperedSignature;

        assertThat(jwtService.isTokenValid(tamperedToken))
                .isFalse();
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt"))
                .isFalse();
    }

    @Test
    void shouldRejectSecretShorterThan32Bytes() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("too-short");
        properties.setExpirationMs(3_600_000);

        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be at least 32 bytes long");
    }
}