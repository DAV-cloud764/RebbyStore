package com.david.rebbystorebackend.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        String name,

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        String sku,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @Size(max = 100, message = "Color must not exceed 100 characters")
        String color,

        @Size(max = 100, message = "Texture must not exceed 100 characters")
        String texture,

        @Size(max = 100, message = "Length must not exceed 100 characters")
        String length,

        @Size(max = 100, message = "Hair type must not exceed 100 characters")
        String hairType,

        @PositiveOrZero(message = "Low stock threshold must not be negative")
        Integer lowStockThreshold
) {
}