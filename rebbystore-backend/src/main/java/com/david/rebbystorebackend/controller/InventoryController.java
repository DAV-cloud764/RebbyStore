package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import com.david.rebbystorebackend.dto.inventory.InventoryAdjustmentRequest;
import com.david.rebbystorebackend.dto.inventory.InventoryLowStockResponse;
import com.david.rebbystorebackend.dto.inventory.InventoryMovementResponse;
import com.david.rebbystorebackend.dto.inventory.InventoryQuantityRequest;
import com.david.rebbystorebackend.dto.inventory.InventoryStockResponse;
import com.david.rebbystorebackend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{productId}/stock-in")
    public ResponseEntity<InventoryMovementResponse> stockIn(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request
    ) {
        InventoryMovement movement = inventoryService.stockIn(
                productId,
                request.quantity()
        );

        return ResponseEntity.ok(
                InventoryMovementResponse.from(movement)
        );
    }

    @PostMapping("/{productId}/stock-out")
    public ResponseEntity<InventoryMovementResponse> stockOut(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request
    ) {
        InventoryMovement movement = inventoryService.stockOut(
                productId,
                request.quantity()
        );

        return ResponseEntity.ok(
                InventoryMovementResponse.from(movement)
        );
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryMovementResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        InventoryMovement movement = inventoryService.adjustStock(
                productId,
                request.quantity(),
                request.increase()
        );

        return ResponseEntity.ok(
                InventoryMovementResponse.from(movement)
        );
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<InventoryStockResponse> getCurrentStock(
            @PathVariable Long productId
    ) {
        int stock = inventoryService.getCurrentStock(productId);

        return ResponseEntity.ok(
                new InventoryStockResponse(
                        productId,
                        stock
                )
        );
    }

    @GetMapping("/{productId}/low-stock")
    public ResponseEntity<InventoryLowStockResponse> isLowStock(
            @PathVariable Long productId
    ) {
        boolean lowStock = inventoryService.isLowStock(productId);

        return ResponseEntity.ok(
                new InventoryLowStockResponse(
                        productId,
                        lowStock
                )
        );
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<InventoryMovementResponse>> getProductMovements(
            @PathVariable Long productId
    ) {
        List<InventoryMovementResponse> response = inventoryService
                .getProductMovements(productId)
                .stream()
                .map(InventoryMovementResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}