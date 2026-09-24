package com.david.rebbystorebackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CorsConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    private CorsConfiguration getConfiguration() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/products");

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();

        return configuration;
    }

    @Test
    void shouldAllowConfiguredFrontendOrigin() {
        CorsConfiguration configuration = getConfiguration();

        assertThat(configuration.checkOrigin("http://localhost:5173"))
                .isEqualTo("http://localhost:5173");
    }

    @Test
    void shouldRejectUnconfiguredOrigin() {
        CorsConfiguration configuration = getConfiguration();

        assertThat(configuration.checkOrigin("http://malicious.example"))
                .isNull();
    }

    @Test
    void shouldConfigureExpectedMethodsAndHeaders() {
        CorsConfiguration configuration = getConfiguration();

        assertThat(configuration.getAllowedMethods())
                .containsExactlyInAnyOrder(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                );

        assertThat(configuration.getAllowedHeaders())
                .containsExactlyInAnyOrder(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                );

        assertThat(configuration.getAllowCredentials())
                .isFalse();
    }
}