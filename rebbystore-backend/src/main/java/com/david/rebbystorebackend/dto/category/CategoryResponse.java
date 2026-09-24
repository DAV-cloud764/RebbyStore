package com.david.rebbystorebackend.dto.category;

import com.david.rebbystorebackend.domain.entity.Category;

import java.time.OffsetDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}