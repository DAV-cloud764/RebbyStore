package com.david.rebbystorebackend.config;

import com.david.rebbystorebackend.security.jwt.JwtProperties;
import com.david.rebbystorebackend.security.jwt.JwtAuthenticationFilter;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import org.springframework.http.HttpMethod;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.Customizer;
import com.david.rebbystorebackend.security.ratelimit.RateLimitFilter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .userDetailsService(userDetailsService)

                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()

                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**"
                        ).permitAll()

                                .requestMatchers("/test/**").permitAll()

                        .requestMatchers("/api/inventory/**")
                        .hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/purchases/**")
                        .hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/suppliers/**")
                        .hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/customers/**")
                        .hasAnyRole("ADMIN", "STAFF")

                        .requestMatchers("/api/reporting/**")
                        .hasAnyRole("ADMIN", "STAFF")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/orders/status/**"
                                )
                                .hasAnyRole("ADMIN", "STAFF")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/orders/**"
                                )
                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

                                // Customer can create an order.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/orders"
                                )
                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

// Customer can add an item to their own pending order.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/orders/*/items"
                                )
                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

// Customer can remove an item from their own pending order.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.DELETE,
                                        "/api/orders/items/*"
                                )
                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

// Customer can read orders, but ownership must be checked
// in the service layer.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.GET,
                                        "/api/orders/**"
                                )
                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

// Workflow transitions belong to staff/admin.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/orders/*/confirm",
                                        "/api/orders/*/process",
                                        "/api/orders/*/ready-for-delivery",
                                        "/api/orders/*/deliver"
                                )
                                .hasAnyRole("ADMIN", "STAFF")

// Cancellation requires ownership for customers.
// We will enforce that in the service layer.
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/orders/*/cancel"
                                )

                                .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Unauthorized"
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Forbidden"
                                        )
                        )
                )

                .httpBasic(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        rateLimitFilter,
                        JwtAuthenticationFilter.class
                );

        return http.build();
    }
}