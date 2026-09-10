package com.david.rebbystorebackend.dto.inventory;

public record InventoryLowStockResponse(
        Long productId,
        boolean lowStock
) {}