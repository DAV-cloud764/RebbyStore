package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.PurchaseItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PurchaseItemRepositoryTest {

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindItemsByPurchaseId() {

        Long supplierId = createSupplier(
                "Purchase Item Supplier",
                "0712342001",
                "purchase-item@example.com"
        );

        Long purchaseId = createPurchase(
                supplierId,
                "PUR-ITEM-001"
        );

        Long categoryId = createCategory(
                "Purchase Item Category",
                "purchase-item-category"
        );

        Long productId = createProduct(
                categoryId,
                "Purchase Item Wig",
                "PURCHASE-ITEM-SKU-001"
        );

        createPurchaseItem(
                purchaseId,
                productId,
                5,
                "100000.00",
                "500000.00"
        );

        createPurchaseItem(
                purchaseId,
                productId,
                3,
                "100000.00",
                "300000.00"
        );

        var results =
                purchaseItemRepository.findByPurchaseId(purchaseId);

        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(PurchaseItem::getProduct)
                .allSatisfy(product ->
                        assertThat(product.getId()).isEqualTo(productId)
                );
    }

    @Test
    void shouldFindItemsByProductId() {

        Long supplierId = createSupplier(
                "Product Purchase Supplier",
                "0712342002",
                "product-purchase@example.com"
        );

        Long purchaseId = createPurchase(
                supplierId,
                "PUR-ITEM-002"
        );

        Long categoryId = createCategory(
                "Product Purchase Category",
                "product-purchase-category"
        );

        Long productId = createProduct(
                categoryId,
                "Product Purchase Wig",
                "PURCHASE-ITEM-SKU-002"
        );

        createPurchaseItem(
                purchaseId,
                productId,
                4,
                "120000.00",
                "480000.00"
        );

        var results =
                purchaseItemRepository.findByProductId(productId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getQuantity()).isEqualTo(4);
        assertThat(results.get(0).getUnitCost())
                .isEqualByComparingTo("120000.00");
        assertThat(results.get(0).getSubtotal())
                .isEqualByComparingTo("480000.00");
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
            String purchaseNumber
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
                "draft",
                java.time.LocalDate.now(),
                new BigDecimal("800000.00")
        );
    }

    private Long createCategory(
            String name,
            String slug
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categories (
                    name,
                    slug
                )
                VALUES (?, ?)
                RETURNING id
                """,
                Long.class,
                name,
                slug
        );
    }

    private Long createProduct(
            Long categoryId,
            String name,
            String sku
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO products (
                    category_id,
                    name,
                    sku,
                    price,
                    stock_quantity,
                    low_stock_threshold,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                categoryId,
                name,
                sku,
                new BigDecimal("150000.00"),
                10,
                5,
                "active"
        );
    }

    private Long createPurchaseItem(
            Long purchaseId,
            Long productId,
            int quantity,
            String unitCost,
            String subtotal
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO purchase_items (
                    purchase_id,
                    product_id,
                    quantity,
                    unit_cost,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                purchaseId,
                productId,
                quantity,
                new BigDecimal(unitCost),
                new BigDecimal(subtotal)
        );
    }
}