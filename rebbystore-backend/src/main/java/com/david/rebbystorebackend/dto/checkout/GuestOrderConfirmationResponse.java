package com.david.rebbystorebackend.dto.checkout;

import com.david.rebbystorebackend.dto.order.OrderItemResponse;
import com.david.rebbystorebackend.dto.order.OrderResponse;

import java.util.List;

/** Limited, entity-free data needed to display an order confirmation. */
public record GuestOrderConfirmationResponse(
        OrderResponse order,
        List<OrderItemResponse> items
) {
    public GuestOrderConfirmationResponse {
        items = List.copyOf(items);
    }
}
