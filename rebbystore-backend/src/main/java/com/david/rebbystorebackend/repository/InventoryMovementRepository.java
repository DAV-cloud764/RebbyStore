package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findByOrderId(Long orderId);

    List<InventoryMovement> findByPurchaseId(Long purchaseId);

    List<InventoryMovement> findByProductId(Long productId);
}