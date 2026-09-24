package com.david.rebbystorebackend.dto.inventory;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;

import java.time.OffsetDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        Long orderId,
        Long purchaseId,
        String type,
        Integer quantity,
        String reason,
        OffsetDateTime createdAt
) {

    public static InventoryMovementResponse from(
            InventoryMovement movement
    ) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getProduct().getSku(),
                movement.getOrder() != null
                        ? movement.getOrder().getId()
                        : null,
                movement.getPurchase() != null
                        ? movement.getPurchase().getId()
                        : null,
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}