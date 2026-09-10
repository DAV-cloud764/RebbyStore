package com.david.rebbystorebackend.dto.inventory;

public record InventoryStockResponse(
        Long productId,
        Integer stockQuantity
) {}