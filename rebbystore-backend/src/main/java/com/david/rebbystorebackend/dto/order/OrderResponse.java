package com.david.rebbystorebackend.dto.order;

import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponse(
        Long id,
        Long customerId,
        String orderNumber,
        String customerName,
        String customerPhone,
        String customerEmail,
        String deliveryAddress,
        String deliveryCity,
        String deliveryRegion,
        String deliveryNotes,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String status,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getDeliveryCity(),
                order.getDeliveryRegion(),
                order.getDeliveryNotes(),
                order.getPaymentMethod(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getStatus().name(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}