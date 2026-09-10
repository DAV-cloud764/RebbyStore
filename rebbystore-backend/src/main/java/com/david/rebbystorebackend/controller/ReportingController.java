package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.dto.reporting.CustomerSummary;
import com.david.rebbystorebackend.dto.reporting.InventorySummary;
import com.david.rebbystorebackend.dto.reporting.OrderSummary;
import com.david.rebbystorebackend.dto.reporting.PurchaseSummary;
import com.david.rebbystorebackend.dto.reporting.SalesSummary;
import com.david.rebbystorebackend.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/sales")
    public ResponseEntity<SalesSummary> getSalesSummary() {
        return ResponseEntity.ok(
                reportingService.getSalesSummary()
        );
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderSummary> getOrderSummary() {
        return ResponseEntity.ok(
                reportingService.getOrderSummary()
        );
    }

    @GetMapping("/purchases")
    public ResponseEntity<PurchaseSummary> getPurchaseSummary() {
        return ResponseEntity.ok(
                reportingService.getPurchaseSummary()
        );
    }

    @GetMapping("/inventory")
    public ResponseEntity<InventorySummary> getInventorySummary() {
        return ResponseEntity.ok(
                reportingService.getInventorySummary()
        );
    }

    @GetMapping("/customers")
    public ResponseEntity<CustomerSummary> getCustomerSummary() {
        return ResponseEntity.ok(
                reportingService.getCustomerSummary()
        );
    }
}