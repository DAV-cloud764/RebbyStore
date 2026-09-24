package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.dto.reporting.CustomerSummary;
import com.david.rebbystorebackend.dto.reporting.InventorySummary;
import com.david.rebbystorebackend.dto.reporting.OrderSummary;
import com.david.rebbystorebackend.dto.reporting.PurchaseSummary;
import com.david.rebbystorebackend.dto.reporting.SalesSummary;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.InventoryMovementRepository;
import com.david.rebbystorebackend.repository.OrderItemRepository;
import com.david.rebbystorebackend.repository.OrderRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseItemRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import com.david.rebbystorebackend.repository.SupplierRepository;
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

@SpringBootTest
@Transactional
class ReportingServiceTest {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementRepository.deleteAll();
        orderItemRepository.deleteAll();
        purchaseItemRepository.deleteAll();
        orderRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        supplierRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Customer createCustomer(
            String phone,
            String email,
            int totalOrders,
            BigDecimal totalSpent
    ) {
        Customer customer = new Customer();

        OffsetDateTime now = OffsetDateTime.now();

        customer.setFullName("Test Customer");
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setTotalOrders(totalOrders);
        customer.setTotalSpent(totalSpent);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        return customerRepository.save(customer);
    }

    private Product createProduct(
            String sku,
            int stock,
            int lowStockThreshold
    ) {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName("Category " + sku);
        category.setSlug("category-" + sku.toLowerCase());
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
        product.setPrice(new BigDecimal("100000"));
        product.setColor("Black");
        product.setTexture("Straight");
        product.setLength("16 inches");
        product.setHairType("Human Hair");
        product.setLowStockThreshold(lowStockThreshold);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        product = productRepository.save(product);

        if (stock > 0) {
            product.increaseStock(stock);
            productRepository.save(product);
        }

        return product;
    }

    private Order createOrder(
            Customer customer,
            OrderStatus status,
            BigDecimal total
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber(
                "ORD-" + System.nanoTime()
        );

        order.setCustomerName(customer.getFullName());
        order.setCustomerPhone(customer.getPhone());
        order.setCustomerEmail(customer.getEmail());

        order.setDeliveryAddress("Test Address");
        order.setDeliveryCity("Ubungo");
        order.setDeliveryRegion("Dar es Salaam");

        order.setPaymentMethod(
                PaymentMethod.CASH_ON_DELIVERY
        );

        order.setSubtotal(total);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setTotal(total);

        order.setStatus(status);

        if (status == OrderStatus.PENDING) {
            order.setExpiresAt(now.plusHours(24));
        } else {
            order.setExpiresAt(null);
        }

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    private Purchase createPurchase(
            Supplier supplier,
            String status,
            BigDecimal totalCost
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        Purchase purchase = new Purchase();

        purchase.setSupplier(supplier);
        purchase.setPurchaseNumber(
                "PUR-" +
                        System.nanoTime()
        );
        purchase.setStatus(status);
        purchase.setPurchaseDate(LocalDate.now());
        purchase.setTotalCost(totalCost);
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);

        return purchaseRepository.save(purchase);
    }

    private Supplier createSupplier() {
        OffsetDateTime now = OffsetDateTime.now();

        Supplier supplier = new Supplier();

        supplier.setName("Test Supplier");
        supplier.setPhone("0712345678");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress("Dar es Salaam");
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    @Test
    void shouldReturnSalesSummary() {
        Customer customer = createCustomer(
                "0712345678",
                "customer@example.com",
                2,
                new BigDecimal("400000")
        );

        createOrder(
                customer,
                OrderStatus.DELIVERED,
                new BigDecimal("200000")
        );

        createOrder(
                customer,
                OrderStatus.DELIVERED,
                new BigDecimal("300000")
        );

        createOrder(
                customer,
                OrderStatus.CANCELLED,
                new BigDecimal("500000")
        );

        SalesSummary summary =
                reportingService.getSalesSummary();

        assertThat(summary.deliveredOrders())
                .isEqualTo(2);

        assertThat(summary.totalRevenue())
                .isEqualByComparingTo("500000");

        assertThat(summary.averageOrderValue())
                .isEqualByComparingTo("250000");
    }

    @Test
    void shouldReturnZeroSalesWhenNoDeliveredOrdersExist() {
        SalesSummary summary =
                reportingService.getSalesSummary();

        assertThat(summary.deliveredOrders())
                .isZero();

        assertThat(summary.totalRevenue())
                .isEqualByComparingTo("0");

        assertThat(summary.averageOrderValue())
                .isEqualByComparingTo("0");
    }

    @Test
    void shouldReturnOrderSummary() {
        Customer customer = createCustomer(
                "0712345678",
                "customer@example.com",
                0,
                BigDecimal.ZERO
        );

        createOrder(
                customer,
                OrderStatus.PENDING,
                new BigDecimal("100000")
        );

        createOrder(
                customer,
                OrderStatus.CONFIRMED,
                new BigDecimal("100000")
        );

        createOrder(
                customer,
                OrderStatus.PROCESSING,
                new BigDecimal("100000")
        );

        createOrder(
                customer,
                OrderStatus.READY_FOR_DELIVERY,
                new BigDecimal("100000")
        );

        createOrder(
                customer,
                OrderStatus.DELIVERED,
                new BigDecimal("100000")
        );

        createOrder(
                customer,
                OrderStatus.CANCELLED,
                new BigDecimal("100000")
        );

        OrderSummary summary =
                reportingService.getOrderSummary();

        assertThat(summary.pending()).isEqualTo(1);
        assertThat(summary.confirmed()).isEqualTo(1);
        assertThat(summary.processing()).isEqualTo(1);
        assertThat(summary.readyForDelivery()).isEqualTo(1);
        assertThat(summary.delivered()).isEqualTo(1);
        assertThat(summary.cancelled()).isEqualTo(1);
        assertThat(summary.total()).isEqualTo(6);
    }

    @Test
    void shouldReturnPurchaseSummary() {
        Supplier supplier = createSupplier();

        createPurchase(
                supplier,
                "received",
                new BigDecimal("500000")
        );

        createPurchase(
                supplier,
                "received",
                new BigDecimal("300000")
        );

        createPurchase(
                supplier,
                "ordered",
                new BigDecimal("700000")
        );

        PurchaseSummary summary =
                reportingService.getPurchaseSummary();

        assertThat(summary.totalPurchases())
                .isEqualTo(3);

        assertThat(summary.receivedPurchases())
                .isEqualTo(2);

        assertThat(summary.totalPurchaseCost())
                .isEqualByComparingTo("800000");
    }

    @Test
    void shouldReturnInventorySummary() {
        createProduct(
                "WIG-001",
                10,
                2
        );

        createProduct(
                "WIG-002",
                2,
                2
        );

        createProduct(
                "WIG-003",
                7,
                3
        );

        InventorySummary summary =
                reportingService.getInventorySummary();

        assertThat(summary.totalProducts())
                .isEqualTo(3);

        assertThat(summary.totalUnitsInStock())
                .isEqualTo(19);

        assertThat(summary.lowStockProducts())
                .isEqualTo(1);
    }

    @Test
    void shouldReturnCustomerSummary() {
        createCustomer(
                "0712345678",
                "one@example.com",
                3,
                new BigDecimal("450000")
        );

        createCustomer(
                "0712345679",
                "two@example.com",
                2,
                new BigDecimal("300000")
        );

        CustomerSummary summary =
                reportingService.getCustomerSummary();

        assertThat(summary.totalCustomers())
                .isEqualTo(2);

        assertThat(summary.totalOrders())
                .isEqualTo(5);

        assertThat(summary.totalSpent())
                .isEqualByComparingTo("750000");
    }
}