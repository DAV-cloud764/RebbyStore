package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.dto.supplier.SupplierCreateRequest;
import com.david.rebbystorebackend.dto.supplier.SupplierResponse;
import com.david.rebbystorebackend.dto.supplier.SupplierUpdateRequest;
import com.david.rebbystorebackend.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> response = supplierService.getAll()
                .stream()
                .map(SupplierResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable Long id
    ) {
        Supplier supplier = supplierService.getById(id);

        return ResponseEntity.ok(
                SupplierResponse.from(supplier)
        );
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SupplierResponse> getSupplierByName(
            @PathVariable String name
    ) {
        Supplier supplier = supplierService.getByName(name);

        return ResponseEntity.ok(
                SupplierResponse.from(supplier)
        );
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierCreateRequest request
    ) {
        Supplier supplier = supplierService.create(
                request.name(),
                request.phone(),
                request.email(),
                request.address()
        );

        return ResponseEntity
                .created(URI.create("/api/suppliers/" + supplier.getId()))
                .body(SupplierResponse.from(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateRequest request
    ) {
        Supplier supplier = supplierService.update(
                id,
                request.name(),
                request.phone(),
                request.email(),
                request.address()
        );

        return ResponseEntity.ok(
                SupplierResponse.from(supplier)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long id
    ) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}