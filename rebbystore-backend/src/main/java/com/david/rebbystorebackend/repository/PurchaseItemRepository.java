package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.PurchaseItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository
        extends JpaRepository<PurchaseItem, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<PurchaseItem> findByPurchaseId(Long purchaseId);

    @EntityGraph(attributePaths = {"product"})
    List<PurchaseItem> findByProductId(Long productId);
}