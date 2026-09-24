package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindItemsByOrderId() {

        Long customerId = createCustomer(
                "Order Item Customer",
                "0761111111",
                "order-item@example.com"
        );

        Long orderId = createOrder(
                customerId,
                "ORD-ITEM-001"
        );

        Long categoryId = createCategory(
                "Order Item Category",
                "order-item-category"
        );

        Long productId = createProduct(
                categoryId,
                "Order Item Wig",
                "ORDER-ITEM-SKU-001"
        );

        createOrderItem(
                orderId,
                productId,
                "Order Item Wig",
                "ORDER-ITEM-SKU-001",
                2,
                "150000.00",
                "300000.00"
        );

        createOrderItem(
                orderId,
                productId,
                "Order Item Wig",
                "ORDER-ITEM-SKU-001",
                1,
                "150000.00",
                "150000.00"
        );

        var results = orderItemRepository.findByOrderId(orderId);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(OrderItem::getSku)
                .containsOnly("ORDER-ITEM-SKU-001");
    }

    @Test
    void shouldFindItemsByProductId() {

        Long customerId = createCustomer(
                "Product Item Customer",
                "0772222222",
                "product-item@example.com"
        );

        Long orderId = createOrder(
                customerId,
                "ORD-ITEM-002"
        );

        Long categoryId = createCategory(
                "Product Item Category",
                "product-item-category"
        );

        Long productId = createProduct(
                categoryId,
                "Product Item Wig",
                "ORDER-ITEM-SKU-002"
        );

        createOrderItem(
                orderId,
                productId,
                "Product Item Wig",
                "ORDER-ITEM-SKU-002",
                3,
                "120000.00",
                "360000.00"
        );

        var results = orderItemRepository.findByProductId(productId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSku())
                .isEqualTo("ORDER-ITEM-SKU-002");
        assertThat(results.get(0).getQuantity())
                .isEqualTo(3);
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
                "Test Customer",
                "0761111111",
                "order-item@example.com",
                "Test Address",
                "Dar es Salaam",
                "Dar es Salaam",
                "cash_on_delivery",
                new BigDecimal("100000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("105000.00"),
                "pending",
                java.time.OffsetDateTime.now().plusHours(24)
        );
    }

    private Long createOrderItem(
            Long orderId,
            Long productId,
            String productName,
            String sku,
            int quantity,
            String unitPrice,
            String subtotal
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO order_items (
                    order_id,
                    product_id,
                    product_name,
                    sku,
                    quantity,
                    unit_price,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                orderId,
                productId,
                productName,
                sku,
                quantity,
                new BigDecimal(unitPrice),
                new BigDecimal(subtotal)
        );
    }
}