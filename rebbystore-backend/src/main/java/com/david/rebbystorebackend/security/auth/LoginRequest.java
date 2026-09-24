package com.david.rebbystorebackend.security.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Username or email is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password

) {}