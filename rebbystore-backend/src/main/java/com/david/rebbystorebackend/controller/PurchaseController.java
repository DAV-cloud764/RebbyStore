package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.PurchaseItem;
import com.david.rebbystorebackend.dto.purchase.PurchaseCreateRequest;
import com.david.rebbystorebackend.dto.purchase.PurchaseItemCreateRequest;
import com.david.rebbystorebackend.dto.purchase.PurchaseItemResponse;
import com.david.rebbystorebackend.dto.purchase.PurchaseResponse;
import com.david.rebbystorebackend.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody PurchaseCreateRequest request
    ) {
        Purchase purchase = purchaseService.create(
                request.supplierId(),
                request.purchaseDate(),
                request.notes()
        );

        return ResponseEntity
                .created(URI.create("/api/purchases/" + purchase.getId()))
                .body(PurchaseResponse.from(purchase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(
            @PathVariable Long id
    ) {
        Purchase purchase = purchaseService.getById(id);

        return ResponseEntity.ok(
                PurchaseResponse.from(purchase)
        );
    }

    @GetMapping("/number/{purchaseNumber}")
    public ResponseEntity<PurchaseResponse> getPurchaseByNumber(
            @PathVariable String purchaseNumber
    ) {
        Purchase purchase = purchaseService.getByPurchaseNumber(purchaseNumber);

        return ResponseEntity.ok(
                PurchaseResponse.from(purchase)
        );
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesBySupplier(
            @PathVariable Long supplierId
    ) {
        List<PurchaseResponse> response = purchaseService
                .getBySupplier(supplierId)
                .stream()
                .map(PurchaseResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByStatus(
            @PathVariable String status
    ) {
        List<PurchaseResponse> response = purchaseService
                .getByStatus(status)
                .stream()
                .map(PurchaseResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<PurchaseItemResponse>> getPurchaseItems(
            @PathVariable Long id
    ) {
        List<PurchaseItemResponse> response = purchaseService
                .getItems(id)
                .stream()
                .map(PurchaseItemResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<PurchaseItemResponse> addPurchaseItem(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseItemCreateRequest request
    ) {
        PurchaseItem item = purchaseService.addItem(
                id,
                request.productId(),
                request.quantity(),
                request.unitCost()
        );

        return ResponseEntity.ok(
                PurchaseItemResponse.from(item)
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removePurchaseItem(
            @PathVariable Long itemId
    ) {
        purchaseService.removeItem(itemId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<PurchaseResponse> orderPurchase(
            @PathVariable Long id
    ) {
        Purchase purchase = purchaseService.order(id);

        return ResponseEntity.ok(
                PurchaseResponse.from(purchase)
        );
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponse> receivePurchase(
            @PathVariable Long id
    ) {
        Purchase purchase = purchaseService.receive(id);

        return ResponseEntity.ok(
                PurchaseResponse.from(purchase)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponse> cancelPurchase(
            @PathVariable Long id
    ) {
        Purchase purchase = purchaseService.cancel(id);

        return ResponseEntity.ok(
                PurchaseResponse.from(purchase)
        );
    }
}