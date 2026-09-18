package com.david.rebbystorebackend.security.auth;

import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.jwt.JwtProperties;
import com.david.rebbystorebackend.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) {
            log.warn("Login attempt rejected: login request is missing");
            throw new IllegalArgumentException("Login request is required");
        }

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.identifier(),
                            request.password()
                    )
            );
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for identifier={}", request.identifier());
            throw ex;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(principal);

        List<String> roles = principal.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        log.info(
                "Login successful: userId={}, username={}, roles={}",
                principal.getId(),
                principal.getUsername(),
                roles
        );

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtProperties.getExpirationMs(),
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                roles
        );
    }
}