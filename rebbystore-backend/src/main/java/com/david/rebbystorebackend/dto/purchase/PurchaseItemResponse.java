package com.david.rebbystorebackend.dto.purchase;

import com.david.rebbystorebackend.domain.entity.PurchaseItem;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal subtotal
) {

    public static PurchaseItemResponse from(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantity(),
                item.getUnitCost(),
                item.getSubtotal()
        );
    }
}