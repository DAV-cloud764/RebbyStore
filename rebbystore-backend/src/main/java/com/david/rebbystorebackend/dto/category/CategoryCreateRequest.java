package com.david.rebbystorebackend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(

        @NotBlank(message = "Category name is required")
        @Size(
                max = 100,
                message = "Category name must not exceed 100 characters"
        )
        String name,

        @NotBlank(message = "Category slug is required")
        @Size(
                max = 100,
                message = "Category slug must not exceed 100 characters"
        )
        String slug,

        @Size(
                max = 1000,
                message = "Category description must not exceed 1000 characters"
        )
        String description
) {
}