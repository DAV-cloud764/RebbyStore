package com.david.rebbystorebackend.dto.customer;

import com.david.rebbystorebackend.domain.entity.Customer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CustomerResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        Integer totalOrders,
        BigDecimal totalSpent,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getTotalOrders(),
                customer.getTotalSpent(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}