package com.david.rebbystorebackend.security.jwt;

import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filterChain = mock(FilterChain.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
        );

        principal = UserPrincipal.createForTesting(
                1L,
                "admin",
                "admin@rebbystore.co.tz",
                "$2a$10$dummy",
                true,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);

        assertThat(
                SecurityContextHolder.getContext().getAuthentication()
        ).isNull();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);

        assertThat(
                SecurityContextHolder.getContext().getAuthentication()
        ).isNull();
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {
        String token = "valid-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(principal);
        assertThat(authentication.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUsername(token);
        verify(userDetailsService).loadUserByUsername("admin");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateInvalidToken() throws Exception {
        String token = "invalid-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(
                SecurityContextHolder.getContext().getAuthentication()
        ).isNull();

        verify(jwtService).isTokenValid(token);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearContextWhenTokenProcessingFails() throws Exception {
        String token = "broken-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid(token))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        filter.doFilter(request, response, filterChain);

        assertThat(
                SecurityContextHolder.getContext().getAuthentication()
        ).isNull();

        verify(jwtService).isTokenValid(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotOverwriteExistingAuthentication() throws Exception {
        var existingAuthentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        String token = "valid-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        assertThat(
                SecurityContextHolder.getContext().getAuthentication()
        ).isSameAs(existingAuthentication);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUsername(token);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldLoadCurrentUserFromDatabase() throws Exception {
        String token = "valid-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        verify(userDetailsService)
                .loadUserByUsername("admin");

        assertThat(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        ).isEqualTo(principal);
    }
}
