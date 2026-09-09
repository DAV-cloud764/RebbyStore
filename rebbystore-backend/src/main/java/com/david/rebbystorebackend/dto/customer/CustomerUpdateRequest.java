package com.david.rebbystorebackend.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(

        @NotBlank(message = "Customer full name is required")
        @Size(
                max = 150,
                message = "Customer full name must not exceed 150 characters"
        )
        String fullName,

        @NotBlank(message = "Customer phone is required")
        @Pattern(
                regexp = "^(?:\\+255|255|0)(?:6|7)\\d{8}$",
                message = "Phone must be a valid Tanzanian mobile number"
        )
        String phone,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        @Size(
                max = 255,
                message = "Customer email must not exceed 255 characters"
        )
        String email
) {
}