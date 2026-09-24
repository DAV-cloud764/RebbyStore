package com.david.rebbystorebackend.security.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidLoginRequest() {
        LoginRequest request = new LoginRequest(
                "admin",
                "Password123!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectBlankIdentifier() {
        LoginRequest request = new LoginRequest(
                "",
                "Password123!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Username or email is required");
    }

    @Test
    void shouldRejectNullIdentifier() {
        LoginRequest request = new LoginRequest(
                null,
                "Password123!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Username or email is required");
    }

    @Test
    void shouldRejectBlankPassword() {
        LoginRequest request = new LoginRequest(
                "admin",
                ""
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Password is required");
    }

    @Test
    void shouldRejectNullPassword() {
        LoginRequest request = new LoginRequest(
                "admin",
                null
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Password is required");
    }

    @Test
    void shouldRejectBothFieldsWhenBlank() {
        LoginRequest request = new LoginRequest(
                "",
                ""
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(2);
    }
}