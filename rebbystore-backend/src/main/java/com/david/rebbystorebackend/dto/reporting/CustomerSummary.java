package com.david.rebbystorebackend.dto.reporting;

import java.math.BigDecimal;

public record CustomerSummary(
        long totalCustomers,
        long totalOrders,
        BigDecimal totalSpent
) {
}