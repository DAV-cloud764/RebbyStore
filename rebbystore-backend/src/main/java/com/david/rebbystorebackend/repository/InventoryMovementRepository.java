package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;


public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByOrderId(Long orderId);

    List<InventoryMovement> findByPurchaseId(Long purchaseId);

    List<InventoryMovement> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product", "order", "purchase"})
    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(
            Long productId
    );

    @EntityGraph(attributePaths = {"product", "order", "purchase"})
    List<InventoryMovement> findAllByOrderByCreatedAtDesc();

}