package com.david.rebbystorebackend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PasswordSecurityTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldHashPasswordAndVerifyCorrectPassword() {
        String rawPassword = "RebbyStore@123";

        String hashedPassword = passwordEncoder.encode(rawPassword);

        assertThat(hashedPassword)
                .isNotEqualTo(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, hashedPassword))
                .isTrue();
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String rawPassword = "RebbyStore@123";

        String hashedPassword = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches("WrongPassword@123", hashedPassword))
                .isFalse();
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "RebbyStore@123";

        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        assertThat(firstHash)
                .isNotEqualTo(secondHash);

        assertThat(passwordEncoder.matches(rawPassword, firstHash))
                .isTrue();

        assertThat(passwordEncoder.matches(rawPassword, secondHash))
                .isTrue();
    }
}