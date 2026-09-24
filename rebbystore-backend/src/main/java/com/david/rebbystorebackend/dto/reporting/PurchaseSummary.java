package com.david.rebbystorebackend.dto.reporting;

import java.math.BigDecimal;

public record PurchaseSummary(
        long totalPurchases,
        long receivedPurchases,
        BigDecimal totalPurchaseCost
) {
}