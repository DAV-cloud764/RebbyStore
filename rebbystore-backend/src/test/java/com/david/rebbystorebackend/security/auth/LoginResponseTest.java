package com.david.rebbystorebackend.security.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseTest {

    @Test
    void shouldCreateLoginResponseWithExpectedValues() {
        LoginResponse response = new LoginResponse(
                "jwt-token",
                "Bearer",
                3_600_000L,
                1L,
                "admin",
                "admin@rebbystore.co.tz",
                List.of("ROLE_ADMIN")
        );

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3_600_000L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.email()).isEqualTo("admin@rebbystore.co.tz");
        assertThat(response.roles()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldNotExposePasswordFields() {
        assertThat(LoginResponse.class.getDeclaredFields())
                .noneMatch(field ->
                        field.getName().equalsIgnoreCase("password")
                                || field.getName().equalsIgnoreCase("passwordHash")
                                || field.getName().equalsIgnoreCase("password_hash")
                );
    }

    @Test
    void shouldSupportMultipleRoles() {
        LoginResponse response = new LoginResponse(
                "jwt-token",
                "Bearer",
                3_600_000L,
                1L,
                "admin",
                "admin@rebbystore.co.tz",
                List.of("ROLE_ADMIN", "ROLE_STAFF")
        );

        assertThat(response.roles())
                .containsExactly("ROLE_ADMIN", "ROLE_STAFF");
    }
}