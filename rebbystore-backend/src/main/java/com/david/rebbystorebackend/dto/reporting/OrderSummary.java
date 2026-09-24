package com.david.rebbystorebackend.dto.reporting;

public record OrderSummary(
        long pending,
        long confirmed,
        long processing,
        long readyForDelivery,
        long delivered,
        long cancelled,
        long total
) {
}