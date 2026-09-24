package com.david.rebbystorebackend.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Pattern(
                regexp = "^(?:\\+255|255|0)7\\d{8}$",
                message = "Phone must be a valid Tanzanian mobile number"
        )
        String phone,

        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address
) {}