package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.AuditActivityLog;
import com.david.rebbystorebackend.service.AuditActivityLogService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.david.rebbystorebackend.dto.reporting.AuditActivitySummary;

@RestController
@RequestMapping("/api/reporting/activities")
@PreAuthorize("hasRole('ADMIN')")
public class AuditActivityLogController {

    private final AuditActivityLogService auditService;

    public AuditActivityLogController(
            AuditActivityLogService auditService
    ) {
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AuditActivitySummary> getMonthlySummary(
            @RequestParam String month
    ) {
        return ResponseEntity.ok(
                auditService.getMonthlySummary(month)
        );
    }

    @GetMapping
    public ResponseEntity<Page<AuditActivityLog>> getMonthlyActivity(
            @RequestParam String month,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditActivityLog> activities =
                auditService.getMonthlyActivity(
                        month,
                        moduleName,
                        outcome,
                        actorUsername,
                        search,
                        page,
                        size
                );

        return ResponseEntity.ok(activities);
    }
}