package com.david.rebbystorebackend.security.auth;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username,
        String email,
        List<String> roles
) {}