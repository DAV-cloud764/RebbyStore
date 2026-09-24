package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class InventoryMovementRepositoryTest {

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindMovementsByProductIdOrderedByCreatedAtDescending() {

        Long categoryId = createCategory(
                "Inventory Ordering Category",
                "inventory-ordering-category"
        );

        Long productId = createProduct(
                categoryId,
                "Inventory Ordering Wig",
                "INVENTORY-ORDERING-SKU-001"
        );

        OffsetDateTime oldest =
                OffsetDateTime.now().minusMinutes(3);

        OffsetDateTime middle =
                OffsetDateTime.now().minusMinutes(2);

        OffsetDateTime newest =
                OffsetDateTime.now().minusMinutes(1);

        createMovementAt(
                productId,
                "in",
                10,
                "adjustment",
                null,
                null,
                oldest
        );

        createMovementAt(
                productId,
                "out",
                2,
                "damaged",
                null,
                null,
                middle
        );

        createMovementAt(
                productId,
                "out",
                1,
                "lost",
                null,
                null,
                newest
        );

        List<InventoryMovement> results =
                inventoryMovementRepository
                        .findByProductIdOrderByCreatedAtDesc(productId);

        assertThat(results).hasSize(3);

        assertThat(results)
                .extracting(InventoryMovement::getReason)
                .containsExactly(
                        "lost",
                        "damaged",
                        "adjustment"
                );
    }

    @Test
    void shouldFindMovementsByOrderId() {

        Long customerId = createCustomer(
                "Inventory Order Customer",
                "0763333333",
                "inventory-order@example.com"
        );

        Long orderId = createOrder(
                customerId,
                "ORD-INVENTORY-001"
        );

        Long categoryId = createCategory(
                "Inventory Order Category",
                "inventory-order-category"
        );

        Long productId = createProduct(
                categoryId,
                "Inventory Order Wig",
                "INVENTORY-ORDER-SKU-001"
        );

        createMovement(
                productId,
                "out",
                2,
                "sale",
                orderId,
                null
        );

        List<InventoryMovement> results =
                inventoryMovementRepository.findByOrderId(orderId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType())
                .isEqualTo("out");
        assertThat(results.get(0).getReason())
                .isEqualTo("sale");
    }

    @Test
    void shouldFindMovementsByPurchaseId() {

        Long supplierId = createSupplier(
                "Inventory Purchase Supplier",
                "0764444444",
                "inventory-purchase@example.com"
        );

        Long purchaseId = createPurchase(
                supplierId,
                "PUR-INVENTORY-001"
        );

        Long categoryId = createCategory(
                "Inventory Purchase Category",
                "inventory-purchase-category"
        );

        Long productId = createProduct(
                categoryId,
                "Inventory Purchase Wig",
                "INVENTORY-PURCHASE-SKU-001"
        );

        createMovement(
                productId,
                "in",
                20,
                "purchase",
                null,
                purchaseId
        );

        List<InventoryMovement> results =
                inventoryMovementRepository.findByPurchaseId(purchaseId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType())
                .isEqualTo("in");
        assertThat(results.get(0).getReason())
                .isEqualTo("purchase");
        assertThat(results.get(0).getQuantity())
                .isEqualTo(20);
        assertThat(results.get(0).getPurchase().getId())
                .isEqualTo(purchaseId);
    }

    @Test
    void shouldFindAllMovementsForProduct() {

        Long categoryId = createCategory(
                "Inventory History Category",
                "inventory-history-category"
        );

        Long productId = createProduct(
                categoryId,
                "Inventory History Wig",
                "INVENTORY-HISTORY-SKU-001"
        );

        createMovement(
                productId,
                "in",
                15,
                "adjustment",
                null,
                null
        );

        createMovement(
                productId,
                "out",
                3,
                "damaged",
                null,
                null
        );

        List<InventoryMovement> results =
                inventoryMovementRepository.findByProductId(productId);

        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(InventoryMovement::getReason)
                .containsExactlyInAnyOrder(
                        "adjustment",
                        "damaged"
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

    private Long createCustomer(
            String fullName,
            String phone,
            String email
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO customers (
                    full_name,
                    phone,
                    email
                )
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                fullName,
                phone,
                email
        );
    }

    private Long createOrder(
            Long customerId,
            String orderNumber
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO orders (
                    customer_id,
                    order_number,
                    customer_name,
                    customer_phone,
                    customer_email,
                    delivery_address,
                    delivery_city,
                    delivery_region,
                    payment_method,
                    subtotal,
                    delivery_fee,
                    total,
                    status,
                    expires_at
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?
                )
                RETURNING id
                """,
                Long.class,
                customerId,
                orderNumber,
                "Inventory Customer",
                "0763333333",
                "inventory-order@example.com",
                "Test Address",
                "Dar es Salaam",
                "Dar es Salaam",
                "cash_on_delivery",
                new BigDecimal("100000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("105000.00"),
                "pending",
                OffsetDateTime.now().plusHours(24)
        );
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
                "received",
                LocalDate.now(),
                new BigDecimal("500000.00")
        );
    }

    private Long createMovement(
            Long productId,
            String type,
            int quantity,
            String reason,
            Long orderId,
            Long purchaseId
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO inventory_movements (
                    product_id,
                    order_id,
                    purchase_id,
                    type,
                    quantity,
                    reason
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                productId,
                orderId,
                purchaseId,
                type,
                quantity,
                reason
        );
    }

    private Long createMovementAt(
            Long productId,
            String type,
            int quantity,
            String reason,
            Long orderId,
            Long purchaseId,
            OffsetDateTime createdAt
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO inventory_movements (
                    product_id,
                    order_id,
                    purchase_id,
                    type,
                    quantity,
                    reason,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                productId,
                orderId,
                purchaseId,
                type,
                quantity,
                reason,
                createdAt
        );
    }
}