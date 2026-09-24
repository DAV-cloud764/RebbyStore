package com.david.rebbystorebackend.domain.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductStatusConverter
        implements AttributeConverter<ProductStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProductStatus status) {
        if (status == null) {
            return null;
        }

        return status.name().toLowerCase();
    }

    @Override
    public ProductStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        return ProductStatus.valueOf(value.toUpperCase());
    }
}