package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository
        extends JpaRepository<Purchase, Long> {

    @EntityGraph(attributePaths = "supplier")
    @Override
    Optional<Purchase> findById(Long id);

    @EntityGraph(attributePaths = "supplier")
    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    boolean existsByPurchaseNumber(String purchaseNumber);

    @EntityGraph(attributePaths = "supplier")
    List<Purchase> findBySupplierId(Long supplierId);

    @EntityGraph(attributePaths = "supplier")
    List<Purchase> findByStatusOrderByPurchaseDateDesc(
            String status
    );
}