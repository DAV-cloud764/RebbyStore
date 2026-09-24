package com.david.rebbystorebackend.security.auth;

import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.jwt.JwtProperties;
import com.david.rebbystorebackend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private AuthService authService;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);

        jwtProperties = new JwtProperties();
        jwtProperties.setExpirationMs(3_600_000L);

        authService = new AuthService(
                authenticationManager,
                jwtService,
                jwtProperties
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
    }

    @Test
    void shouldAuthenticateAndGenerateToken() {
        LoginRequest request = new LoginRequest(
                "admin",
                "Password123!"
        );

        Authentication authenticated =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        when(jwtService.generateToken(principal))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3_600_000L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.email()).isEqualTo("admin@rebbystore.co.tz");
        assertThat(response.roles())
                .containsExactly("ROLE_ADMIN");

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).generateToken(principal);
    }

    @Test
    void shouldPassIdentifierAndPasswordToAuthenticationManager() {
        LoginRequest request = new LoginRequest(
                "admin",
                "Password123!"
        );

        Authentication authenticated =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        when(jwtService.generateToken(principal))
                .thenReturn("jwt-token");

        authService.login(request);

        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(captor.capture());

        Authentication authentication = captor.getValue();

        assertThat(authentication.getName()).isEqualTo("admin");
        assertThat(authentication.getCredentials())
                .isEqualTo("Password123!");
    }

    @Test
    void shouldPropagateAuthenticationFailure() {
        LoginRequest request = new LoginRequest(
                "admin",
                "WrongPassword"
        );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad credentials");

        verify(authenticationManager)
                .authenticate(any(Authentication.class));

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectNullLoginRequest() {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Login request is required");

        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void shouldReturnAllAssignedRoles() {
        UserPrincipal multiRolePrincipal =
                UserPrincipal.createForTesting(
                        2L,
                        "manager",
                        "manager@rebbystore.co.tz",
                        "$2a$10$dummy",
                        true,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_STAFF")
                        )
                );

        Authentication authenticated =
                new UsernamePasswordAuthenticationToken(
                        multiRolePrincipal,
                        null,
                        multiRolePrincipal.getAuthorities()
                );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        when(jwtService.generateToken(multiRolePrincipal))
                .thenReturn("manager-token");

        LoginResponse response = authService.login(
                new LoginRequest("manager", "Password123!")
        );

        assertThat(response.roles())
                .containsExactly("ROLE_ADMIN", "ROLE_STAFF");
    }
}