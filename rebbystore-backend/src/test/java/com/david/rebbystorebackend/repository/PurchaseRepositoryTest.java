package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Purchase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PurchaseRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindPurchaseByPurchaseNumber() {

        Long supplierId = createSupplier(
                "Purchase Test Supplier",
                "0712341001",
                "purchase-supplier@example.com"
        );

        Long purchaseId = createPurchase(
                supplierId,
                "PUR-TEST-001",
                "draft",
                LocalDate.now(),
                "250000.00"
        );

        var result =
                purchaseRepository.findByPurchaseNumber("PUR-TEST-001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(purchaseId);
        assertThat(result.get().getPurchaseNumber())
                .isEqualTo("PUR-TEST-001");
    }

    @Test
    void shouldCheckWhetherPurchaseNumberExists() {

        Long supplierId = createSupplier(
                "Exists Purchase Supplier",
                "0712341002",
                "exists-purchase@example.com"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-002",
                "draft",
                LocalDate.now(),
                "300000.00"
        );

        assertThat(
                purchaseRepository.existsByPurchaseNumber(
                        "PUR-TEST-002"
                )
        ).isTrue();

        assertThat(
                purchaseRepository.existsByPurchaseNumber(
                        "PUR-NOT-FOUND"
                )
        ).isFalse();
    }

    @Test
    void shouldFindPurchasesBySupplierId() {

        Long supplierId = createSupplier(
                "Supplier History Test",
                "0712341003",
                "supplier-history@example.com"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-003",
                "ordered",
                LocalDate.now(),
                "400000.00"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-004",
                "received",
                LocalDate.now(),
                "500000.00"
        );

        var results =
                purchaseRepository.findBySupplierId(supplierId);

        assertThat(results)
                .extracting(Purchase::getPurchaseNumber)
                .containsExactlyInAnyOrder(
                        "PUR-TEST-003",
                        "PUR-TEST-004"
                );
    }

    @Test
    void shouldFindPurchasesByStatusOrderedByPurchaseDateDescending() {

        Long supplierId = createSupplier(
                "Purchase Status Supplier",
                "0712341004",
                "purchase-status@example.com"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-005",
                "received",
                LocalDate.of(2026, 9, 1),
                "600000.00"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-006",
                "received",
                LocalDate.of(2026, 9, 5),
                "700000.00"
        );

        createPurchase(
                supplierId,
                "PUR-TEST-007",
                "draft",
                LocalDate.of(2026, 9, 7),
                "800000.00"
        );

        List<Purchase> results =
                purchaseRepository
                        .findByStatusOrderByPurchaseDateDesc("received");

        assertThat(results).hasSize(2);

        assertThat(results.get(0).getPurchaseNumber())
                .isEqualTo("PUR-TEST-006");

        assertThat(results.get(1).getPurchaseNumber())
                .isEqualTo("PUR-TEST-005");
    }

    private Long createSupplier(
            String name,
            String phone,
            String email
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO suppliers (
                    name,
                    phone,
                    email
                )
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                name,
                phone,
                email
        );
    }

    private Long createPurchase(
            Long supplierId,
            String purchaseNumber,
            String status,
            LocalDate purchaseDate,
            String totalCost
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO purchases (
                    supplier_id,
                    purchase_number,
                    status,
                    purchase_date,
                    total_cost
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                supplierId,
                purchaseNumber,
                status,
                purchaseDate,
                new BigDecimal(totalCost)
        );
    }
}