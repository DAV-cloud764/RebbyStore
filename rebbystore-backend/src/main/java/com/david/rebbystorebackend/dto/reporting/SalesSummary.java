package com.david.rebbystorebackend.dto.reporting;

import java.math.BigDecimal;

public record SalesSummary(
        long deliveredOrders,
        BigDecimal totalRevenue,
        BigDecimal averageOrderValue
) {
}