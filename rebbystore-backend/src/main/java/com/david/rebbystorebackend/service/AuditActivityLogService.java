package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.AuditActivityLog;
import com.david.rebbystorebackend.repository.AuditActivityLogRepository;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.david.rebbystorebackend.dto.reporting.AuditActivitySummary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
@Transactional(readOnly = true)
public class AuditActivityLogService {

    private static final ZoneId REPORTING_ZONE =
            ZoneId.of("Africa/Dar_es_Salaam");

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditActivityLogRepository auditRepository;

    public AuditActivityLogService(
            AuditActivityLogRepository auditRepository
    ) {
        this.auditRepository = auditRepository;
    }

    public Page<AuditActivityLog> getMonthlyActivity(
            String month,
            String moduleName,
            String outcome,
            String actorUsername,
            String search,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        int pageSize = size <= 0
                ? DEFAULT_PAGE_SIZE
                : Math.min(size, MAX_PAGE_SIZE);

        YearMonth selectedMonth = parseMonth(month);

        OffsetDateTime start = selectedMonth
                .atDay(1)
                .atStartOfDay(REPORTING_ZONE)
                .toOffsetDateTime();

        OffsetDateTime end = selectedMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(REPORTING_ZONE)
                .toOffsetDateTime();

        return auditRepository.searchActivityLogs(
                start,
                end,
                normalizeFilter(moduleName),
                normalizeOutcome(outcome),
                normalizeFilter(actorUsername),
                normalizeFilter(search),
                PageRequest.of(page, pageSize)
        );
    }

    public AuditActivitySummary getMonthlySummary(String month) {
        YearMonth selectedMonth = parseMonth(month);

        OffsetDateTime start = selectedMonth
                .atDay(1)
                .atStartOfDay(REPORTING_ZONE)
                .toOffsetDateTime();

        OffsetDateTime end = selectedMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(REPORTING_ZONE)
                .toOffsetDateTime();

        long total = auditRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        start, end
                );

        long successful = auditRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndOutcome(
                        start, end, "SUCCESS"
                );

        long failed = auditRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndOutcome(
                        start, end, "FAILURE"
                );

        Map<String, Long> activitiesByModule = new LinkedHashMap<>();

        List<Object[]> moduleCounts =
                auditRepository.countActivitiesByModuleBetween(start, end);

        for (Object[] row : moduleCounts) {
            String moduleName = (String) row[0];
            long count = ((Number) row[1]).longValue();

            activitiesByModule.put(moduleName, count);
        }

        return new AuditActivitySummary(
                selectedMonth.toString(),
                total,
                successful,
                failed,
                activitiesByModule
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordActivity(
            String actorUsername,
            String actorRoles,
            String moduleName,
            String action,
            String httpMethod,
            String requestPath,
            int httpStatus,
            String ipAddress,
            long durationMs
    ) {
        AuditActivityLog activity = new AuditActivityLog();

        activity.setActorUsername(actorUsername);
        activity.setActorRoles(actorRoles);
        activity.setModuleName(moduleName);
        activity.setAction(action);
        activity.setHttpMethod(httpMethod);
        activity.setRequestPath(requestPath);
        activity.setHttpStatus(httpStatus);

        activity.setOutcome(
                httpStatus >= 400 ? "FAILURE" : "SUCCESS"
        );

        activity.setIpAddress(ipAddress);
        activity.setDurationMs(Math.max(0, durationMs));

        activity.setCreatedAt(
                OffsetDateTime.now(REPORTING_ZONE)
        );

        auditRepository.save(activity);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException(
                    "Month is required in YYYY-MM format"
            );
        }

        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid month. Use YYYY-MM, for example 2026-10"
            );
        }
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeOutcome(String outcome) {
        if (outcome == null || outcome.isBlank()) {
            return null;
        }

        String normalized = outcome.trim().toUpperCase();

        if (!normalized.equals("SUCCESS")
                && !normalized.equals("FAILURE")) {
            throw new IllegalArgumentException(
                    "Outcome must be SUCCESS or FAILURE"
            );
        }

        return normalized;
    }
}