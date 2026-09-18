package com.david.rebbystorebackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;


import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest("GET", "/api/test");

        request = mockRequest;
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        var response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid request"),
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(400);

        ApiErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Bad Request");
        assertThat(body.message()).isEqualTo("Invalid request");
        assertThat(body.path()).isEqualTo("/api/test");
        assertThat(body.fieldErrors()).isEmpty();
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        var response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Product not found"),
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(404);

        ApiErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isEqualTo("Product not found");
        assertThat(body.path()).isEqualTo("/api/test");
        assertThat(body.fieldErrors()).isEmpty();
    }

    @Test
    void shouldHandleConflictException() {
        var response = handler.handleConflictException(
                new ConflictException("Resource already exists"),
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(409);

        ApiErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.error()).isEqualTo("Conflict");
        assertThat(body.message()).isEqualTo("Resource already exists");
        assertThat(body.path()).isEqualTo("/api/test");
        assertThat(body.fieldErrors()).isEmpty();
    }

    @Test
    void shouldHandleAuthenticationException() {
        var exception =
                new org.springframework.security.authentication.BadCredentialsException(
                        "Authentication failed"
                );

        var response = handler.handleAuthenticationException(
                exception,
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(401);

        ApiErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(401);
        assertThat(body.error()).isEqualTo("Unauthorized");
        assertThat(body.message())
                .isEqualTo("Invalid username/email or password");
        assertThat(body.path()).isEqualTo("/api/test");
        assertThat(body.fieldErrors()).isEmpty();
    }

    @Test
    void shouldHandleUnexpectedExceptionWithoutExposingInternalDetails() {
        Exception exception =
                new RuntimeException("Database password leaked internally");

        var response = handler.handleGenericException(
                exception,
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(500);

        ApiErrorResponse body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message())
                .isEqualTo("An unexpected error occurred");
        assertThat(body.message())
                .doesNotContain("Database password");
        assertThat(body.path()).isEqualTo("/api/test");
        assertThat(body.fieldErrors()).isEmpty();
    }
}