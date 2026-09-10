package com.david.rebbystorebackend.dto.order;

import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotBlank(message = "Delivery address is required")
        @Size(max = 255, message = "Delivery address must not exceed 255 characters")
        String deliveryAddress,

        @NotBlank(message = "Delivery city is required")
        @Size(max = 100, message = "Delivery city must not exceed 100 characters")
        String deliveryCity,

        @NotBlank(message = "Delivery region is required")
        @Size(max = 100, message = "Delivery region must not exceed 100 characters")
        String deliveryRegion,

        @Size(max = 2000, message = "Delivery notes must not exceed 2000 characters")
        String deliveryNotes,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}