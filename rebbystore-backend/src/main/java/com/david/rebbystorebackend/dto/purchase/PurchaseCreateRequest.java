package com.david.rebbystorebackend.dto.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PurchaseCreateRequest(

        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotNull(message = "Purchase date is required")
        LocalDate purchaseDate,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes
) {}