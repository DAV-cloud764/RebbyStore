package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    boolean existsByPurchaseNumber(String purchaseNumber);

    List<Purchase> findBySupplierId(Long supplierId);

    List<Purchase> findByStatusOrderByPurchaseDateDesc(String status);
}