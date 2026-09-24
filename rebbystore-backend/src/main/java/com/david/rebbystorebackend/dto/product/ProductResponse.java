package com.david.rebbystorebackend.dto.product;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String name,
        String sku,
        String description,
        BigDecimal price,
        String color,
        String texture,
        String length,
        String hairType,
        Integer stockQuantity,
        Integer lowStockThreshold,
        ProductStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getColor(),
                product.getTexture(),
                product.getLength(),
                product.getHairType(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}