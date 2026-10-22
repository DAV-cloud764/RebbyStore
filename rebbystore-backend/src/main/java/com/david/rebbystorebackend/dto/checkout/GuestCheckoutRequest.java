package com.david.rebbystorebackend.dto.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Validated request body for guest checkout.
 * Prices are intentionally not accepted from the client; the backend
 * must calculate order prices from the current product records.
 */
public record GuestCheckoutRequest(

        @NotNull(message = "Customer information is required")
        @Valid
        Customer customer,

        @NotNull(message = "Delivery information is required")
        @Valid
        Delivery delivery,

        @NotEmpty(message = "At least one item is required")
        @Size(max = 50, message = "An order cannot contain more than 50 item entries")
        List<@NotNull @Valid Item> items
) {

    public record Customer(

            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must not exceed 150 characters")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Size(max = 20, message = "Phone number must not exceed 20 characters")
            @Pattern(
                    regexp = "^(?:\\+?255|0)[\\s()-]*(?:6|7)(?:[\\s()-]*\\d){8}$",
                    message = "Enter a valid Tanzanian mobile number"
            )
            String phone,

            @NotBlank(message = "Email address is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 255, message = "Email must not exceed 255 characters")
            String email
    ) {
    }

    public record Delivery(

            @NotBlank(message = "Delivery address is required")
            @Size(max = 255, message = "Delivery address must not exceed 255 characters")
            String address,

            @NotBlank(message = "City is required")
            @Size(max = 100, message = "City must not exceed 100 characters")
            String city,

            @NotBlank(message = "Region is required")
            @Size(max = 100, message = "Region must not exceed 100 characters")
            String region,

            @Size(max = 2000, message = "Delivery notes must not exceed 2000 characters")
            String notes
    ) {
    }

    public record Item(

            @NotNull(message = "Product ID is required")
            @Positive(message = "Product ID must be positive")
            Long productId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be greater than zero")
            @Max(value = 999, message = "Quantity must not exceed 999 per product")
            Integer quantity
    ) {
    }
}
