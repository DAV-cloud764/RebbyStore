package com.david.rebbystorebackend.dto.reporting;

public record InventorySummary(
        long totalProducts,
        long totalUnitsInStock,
        long lowStockProducts
) {
}