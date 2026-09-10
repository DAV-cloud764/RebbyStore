package com.david.rebbystorebackend.dto.purchase;

import com.david.rebbystorebackend.domain.entity.Purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PurchaseResponse(
        Long id,
        Long supplierId,
        String supplierName,
        String purchaseNumber,
        String status,
        LocalDate purchaseDate,
        BigDecimal totalCost,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getSupplier().getId(),
                purchase.getSupplier().getName(),
                purchase.getPurchaseNumber(),
                purchase.getStatus(),
                purchase.getPurchaseDate(),
                purchase.getTotalCost(),
                purchase.getNotes(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }
}