package com.david.rebbystorebackend.dto.order;

import com.david.rebbystorebackend.domain.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}