package com.david.rebbystorebackend.dto.reporting;

import java.util.Map;

public record AuditActivitySummary(
        String month,
        long totalActivities,
        long successfulActivities,
        long failedActivities,
        Map<String, Long> activitiesByModule
) {
}