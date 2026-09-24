package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindOrderByOrderNumber() {

        Long customerId = createCustomer(
                "Order Test Customer",
                "0711111111",
                "order-test@example.com"
        );

        Long orderId = createOrder(
                customerId,
                "ORD-TEST-001",
                "pending",
                OffsetDateTime.now().plusHours(24)
        );

        var result = orderRepository.findByOrderNumber("ORD-TEST-001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(orderId);
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD-TEST-001");
    }

    @Test
    void shouldCheckWhetherOrderNumberExists() {

        Long customerId = createCustomer(
                "Exists Test Customer",
                "0722222222",
                "exists-test@example.com"
        );

        createOrder(
                customerId,
                "ORD-TEST-002",
                "pending",
                OffsetDateTime.now().plusHours(24)
        );

        assertThat(orderRepository.existsByOrderNumber("ORD-TEST-002"))
                .isTrue();

        assertThat(orderRepository.existsByOrderNumber("ORD-NOT-FOUND"))
                .isFalse();
    }

    @Test
    void shouldFindOrdersByCustomerId() {

        Long customerId = createCustomer(
                "Customer Orders Test",
                "0733333333",
                "customer-orders@example.com"
        );

        createOrder(
                customerId,
                "ORD-TEST-003",
                "pending",
                OffsetDateTime.now().plusHours(24)
        );

        createOrder(
                customerId,
                "ORD-TEST-004",
                "confirmed",
                null
        );

        var results = orderRepository.findByCustomerId(customerId);

        assertThat(results)
                .extracting(Order::getOrderNumber)
                .contains("ORD-TEST-003", "ORD-TEST-004");
    }

    @Test
    void shouldFindOrdersByStatus() {

        Long customerId = createCustomer(
                "Status Test Customer",
                "0744444444",
                "status-test@example.com"
        );

        createOrder(
                customerId,
                "ORD-TEST-005",
                "pending",
                OffsetDateTime.now().plusHours(24)
        );

        createOrder(
                customerId,
                "ORD-TEST-006",
                "delivered",
                null
        );

        var results = orderRepository.findByStatus(OrderStatus.PENDING);

        assertThat(results)
                .extracting(Order::getOrderNumber)
                .contains("ORD-TEST-005")
                .doesNotContain("ORD-TEST-006");
    }

    @Test
    void shouldFindPendingOrdersThatHaveExpired() {

        Long customerId = createCustomer(
                "Expiry Test Customer",
                "0755555555",
                "expiry-test@example.com"
        );

        OffsetDateTime pastExpiry = OffsetDateTime.now().minusHours(1);
        OffsetDateTime futureExpiry = OffsetDateTime.now().plusHours(1);

        createOrder(
                customerId,
                "ORD-TEST-007",
                "pending",
                pastExpiry
        );

        createOrder(
                customerId,
                "ORD-TEST-008",
                "pending",
                futureExpiry
        );

        createOrder(
                customerId,
                "ORD-TEST-009",
                "confirmed",
                pastExpiry
        );

        var results = orderRepository.findByStatusAndExpiresAtBefore(
                OrderStatus.PENDING,
                OffsetDateTime.now()
        );

        assertThat(results)
                .extracting(Order::getOrderNumber)
                .contains("ORD-TEST-007")
                .doesNotContain("ORD-TEST-008", "ORD-TEST-009");
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
            String orderNumber,
            String status,
            OffsetDateTime expiresAt
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
                "0711111111",
                "test@example.com",
                "Test Address",
                "Dar es Salaam",
                "Dar es Salaam",
                "cash_on_delivery",
                new BigDecimal("100000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("105000.00"),
                status,
                expiresAt
        );
    }
}