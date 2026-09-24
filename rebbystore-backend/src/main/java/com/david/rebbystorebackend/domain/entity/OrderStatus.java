package com.david.rebbystorebackend.domain.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    READY_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}