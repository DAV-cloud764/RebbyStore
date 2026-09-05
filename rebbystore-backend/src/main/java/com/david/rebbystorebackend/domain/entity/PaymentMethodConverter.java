package com.david.rebbystorebackend.domain.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentMethodConverter
        implements AttributeConverter<PaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        return switch (paymentMethod) {
            case CASH_ON_DELIVERY -> "cash_on_delivery";
        };
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case "cash_on_delivery" -> PaymentMethod.CASH_ON_DELIVERY;
            default -> throw new IllegalArgumentException(
                    "Unknown payment method: " + value
            );
        };
    }
}