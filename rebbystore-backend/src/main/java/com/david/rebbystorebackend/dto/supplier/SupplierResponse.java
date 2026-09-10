package com.david.rebbystorebackend.dto.supplier;

import com.david.rebbystorebackend.domain.entity.Supplier;

import java.time.OffsetDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}