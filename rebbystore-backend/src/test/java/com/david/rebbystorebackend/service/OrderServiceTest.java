package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.*;
import com.david.rebbystorebackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Customer createCustomer() {
        Customer customer = new Customer();

        OffsetDateTime now = OffsetDateTime.now();

        customer.setFullName("John Doe");
        customer.setPhone("0712345678");
        customer.setEmail("john@example.com");
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        return customerRepository.save(customer);
    }

    private Product createProduct(
            String sku,
            BigDecimal price,
            int stock
    ) {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName("Human Hair " + sku);
        category.setSlug("human-hair-" + sku.toLowerCase());
        category.setDescription("Test category");
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        category = categoryRepository.save(category);

        Product product = new Product();

        product.setCategory(category);
        product.setName("Test Wig");
        product.setSku(sku);
        product.setDescription("Test product");
        product.setPrice(price);
        product.setColor("Black");
        product.setTexture("Straight");
        product.setLength("16 inches");
        product.setHairType("Human Hair");
        product.setLowStockThreshold(2);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        Product saved = productRepository.save(product);

        if (stock > 0) {
            saved.increaseStock(stock);
            productRepository.save(saved);
        }

        return saved;
    }

    private Order createBasicOrder(Customer customer) {
        return orderService.createOrder(
                customer.getId(),
                "123 Main Street",
                "Ubungo",
                "Dar es Salaam",
                null,
                PaymentMethod.CASH_ON_DELIVERY
        );
    }

    @Test
    void shouldCreatePendingOrder() {
        Customer customer = createCustomer();

        Order order = createBasicOrder(customer);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderNumber()).startsWith("ORD-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentMethod())
                .isEqualTo(PaymentMethod.CASH_ON_DELIVERY);

        assertThat(order.getCustomerName())
                .isEqualTo("John Doe");
        assertThat(order.getCustomerPhone())
                .isEqualTo("0712345678");
        assertThat(order.getCustomerEmail())
                .isEqualTo("john@example.com");

        assertThat(order.getExpiresAt()).isNotNull();

        assertThat(order.getSubtotal())
                .isEqualByComparingTo("0");

        assertThat(order.getDeliveryFee())
                .isEqualByComparingTo("5000");

        assertThat(order.getTotal())
                .isEqualByComparingTo("5000");
    }

    @Test
    void shouldAddItemAndCalculateSubtotalAndDeliveryFee() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        OrderItem item = orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice())
                .isEqualByComparingTo("100000");
        assertThat(item.getSubtotal())
                .isEqualByComparingTo("200000");

        Order updated =
                orderService.getById(order.getId());

        assertThat(updated.getSubtotal())
                .isEqualByComparingTo("200000");

        assertThat(updated.getDeliveryFee())
                .isEqualByComparingTo("0");

        assertThat(updated.getTotal())
                .isEqualByComparingTo("200000");
    }

    @Test
    void shouldChargeDeliveryFeeBelowThreshold() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                1
        );

        Order updated =
                orderService.getById(order.getId());

        assertThat(updated.getSubtotal())
                .isEqualByComparingTo("100000");

        assertThat(updated.getDeliveryFee())
                .isEqualByComparingTo("5000");

        assertThat(updated.getTotal())
                .isEqualByComparingTo("105000");
    }

    @Test
    void shouldNotDeductStockWhenAddingItem() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                3
        );

        Product unchanged =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(unchanged.getStockQuantity())
                .isEqualTo(10);
    }

    @Test
    void shouldConfirmOrderAndDeductStock() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                3
        );

        Order confirmed =
                orderService.confirm(order.getId());

        assertThat(confirmed.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);

        Product updated =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(updated.getStockQuantity())
                .isEqualTo(7);

        List<InventoryMovement> movements =
                inventoryMovementRepository
                        .findByOrderId(order.getId());

        assertThat(movements).hasSize(1);

        InventoryMovement movement = movements.get(0);

        assertThat(movement.getType())
                .isEqualTo("out");

        assertThat(movement.getQuantity())
                .isEqualTo(3);

        assertThat(movement.getReason())
                .isEqualTo("sale");

        assertThat(movement.getOrder().getId())
                .isEqualTo(order.getId());
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                2
        );

        Order order = createBasicOrder(customer);

        assertThatThrownBy(() ->
                orderService.addItem(
                        order.getId(),
                        product.getId(),
                        3
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldRejectConfirmWhenStockChangedAfterItemWasAdded() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                5
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                5
        );

        product = productRepository.findById(product.getId())
                .orElseThrow();

        product.decreaseStock(3);
        productRepository.save(product);

        assertThatThrownBy(() ->
                orderService.confirm(order.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldProcessConfirmedOrder() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        orderService.confirm(order.getId());

        Order processed =
                orderService.process(order.getId());

        assertThat(processed.getStatus())
                .isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldMarkOrderReadyForDelivery() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        orderService.confirm(order.getId());
        orderService.process(order.getId());

        Order ready =
                orderService.markReadyForDelivery(
                        order.getId()
                );

        assertThat(ready.getStatus())
                .isEqualTo(OrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void shouldDeliverOrderAndUpdateCustomerTotals() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        orderService.confirm(order.getId());
        orderService.process(order.getId());
        orderService.markReadyForDelivery(order.getId());

        Order delivered =
                orderService.markDelivered(
                        order.getId()
                );

        assertThat(delivered.getStatus())
                .isEqualTo(OrderStatus.DELIVERED);

        Customer updatedCustomer =
                customerRepository.findById(customer.getId())
                        .orElseThrow();

        assertThat(updatedCustomer.getTotalOrders())
                .isEqualTo(1);

        assertThat(updatedCustomer.getTotalSpent())
                .isEqualByComparingTo("200000");
    }

    @Test
    void shouldCancelPendingOrderWithoutChangingStock() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        Order cancelled =
                orderService.cancel(order.getId());

        assertThat(cancelled.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        Product updated =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(updated.getStockQuantity())
                .isEqualTo(10);

        assertThat(
                inventoryMovementRepository
                        .findByOrderId(order.getId())
        ).isEmpty();
    }

    @Test
    void shouldCancelConfirmedOrderAndRestoreStock() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                3
        );

        orderService.confirm(order.getId());

        assertThat(
                productRepository.findById(product.getId())
                        .orElseThrow()
                        .getStockQuantity()
        ).isEqualTo(7);

        Order cancelled =
                orderService.cancel(order.getId());

        assertThat(cancelled.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        Product restored =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(restored.getStockQuantity())
                .isEqualTo(10);

        List<InventoryMovement> movements =
                inventoryMovementRepository
                        .findByOrderId(order.getId());

        assertThat(movements).hasSize(2);

        assertThat(
                movements.stream()
                        .filter(m -> "out".equals(m.getType()))
                        .count()
        ).isEqualTo(1);

        assertThat(
                movements.stream()
                        .filter(m -> "in".equals(m.getType()))
                        .count()
        ).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Customer customer = createCustomer();

        Order order = createBasicOrder(customer);

        assertThatThrownBy(() ->
                orderService.process(order.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be in status");
    }

    @Test
    void shouldNotAllowOrderChangesAfterConfirmation() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        OrderItem item = orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        orderService.confirm(order.getId());

        assertThatThrownBy(() ->
                orderService.removeItem(item.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be in status");
    }

    @Test
    void shouldSnapshotCustomerData() {
        Customer customer = createCustomer();

        Order order = createBasicOrder(customer);

        customer.setFullName("Changed Name");
        customer.setPhone("0712345679");
        customer.setEmail("changed@example.com");
        customerRepository.save(customer);

        Order unchanged =
                orderService.getById(order.getId());

        assertThat(unchanged.getCustomerName())
                .isEqualTo("John Doe");

        assertThat(unchanged.getCustomerPhone())
                .isEqualTo("0712345678");

        assertThat(unchanged.getCustomerEmail())
                .isEqualTo("john@example.com");
    }

    @Test
    void shouldRejectMissingDeliveryAddress() {
        Customer customer = createCustomer();

        assertThatThrownBy(() ->
                orderService.createOrder(
                        customer.getId(),
                        "   ",
                        "Ubungo",
                        "Dar es Salaam",
                        null,
                        PaymentMethod.CASH_ON_DELIVERY
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Delivery address must not be blank"
                );
    }

    @Test
    void shouldRejectMissingPaymentMethod() {
        Customer customer = createCustomer();

        assertThatThrownBy(() ->
                orderService.createOrder(
                        customer.getId(),
                        "123 Main Street",
                        "Ubungo",
                        "Dar es Salaam",
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Payment method must not be null"
                );
    }

    @Test
    void shouldExpirePendingOrder() {
        Customer customer = createCustomer();

        Order order = createBasicOrder(customer);

        order.setExpiresAt(
                OffsetDateTime.now().minusHours(1)
        );

        orderRepository.save(order);

        int expired =
                orderService.expirePendingOrders();

        assertThat(expired).isEqualTo(1);

        Order updated =
                orderService.getById(order.getId());

        assertThat(updated.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);

        assertThat(updated.getExpiresAt())
                .isNull();
    }

    @Test
    void shouldNotConfirmExpiredOrder() {
        Customer customer = createCustomer();

        Product product = createProduct(
                "WIG-001",
                new BigDecimal("100000"),
                10
        );

        Order order = createBasicOrder(customer);

        orderService.addItem(
                order.getId(),
                product.getId(),
                2
        );

        order.setExpiresAt(
                OffsetDateTime.now().minusHours(1)
        );

        orderRepository.save(order);

        assertThatThrownBy(() ->
                orderService.confirm(order.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Order has expired"
                );
    }
}