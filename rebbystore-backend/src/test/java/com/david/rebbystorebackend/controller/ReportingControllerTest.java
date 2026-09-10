package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.OrderRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        supplierRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldReturnSalesSummary() throws Exception {
        Customer customer = createCustomer(
                "Sales Customer",
                "0712345678",
                "sales@example.com"
        );

        createOrder(
                customer,
                "ORD-SALES01",
                OrderStatus.DELIVERED,
                new BigDecimal("200000.00")
        );

        createOrder(
                customer,
                "ORD-SALES02",
                OrderStatus.DELIVERED,
                new BigDecimal("300000.00")
        );

        createOrder(
                customer,
                "ORD-SALES03",
                OrderStatus.PENDING,
                new BigDecimal("100000.00")
        );

        mockMvc.perform(get("/api/reporting/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveredOrders").value(2))
                .andExpect(jsonPath("$.totalRevenue").value(500000.00))
                .andExpect(jsonPath("$.averageOrderValue").value(250000.00));
    }

    @Test
    void shouldReturnZeroSalesWhenThereAreNoDeliveredOrders()
            throws Exception {

        mockMvc.perform(get("/api/reporting/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveredOrders").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.averageOrderValue").value(0));
    }

    @Test
    void shouldReturnOrderSummary() throws Exception {
        Customer customer = createCustomer(
                "Order Customer",
                "0723456789",
                "orders@example.com"
        );

        createOrder(
                customer,
                "ORD-01",
                OrderStatus.PENDING,
                new BigDecimal("100000.00")
        );

        createOrder(
                customer,
                "ORD-02",
                OrderStatus.CONFIRMED,
                new BigDecimal("120000.00")
        );

        createOrder(
                customer,
                "ORD-03",
                OrderStatus.PROCESSING,
                new BigDecimal("130000.00")
        );

        createOrder(
                customer,
                "ORD-04",
                OrderStatus.READY_FOR_DELIVERY,
                new BigDecimal("140000.00")
        );

        createOrder(
                customer,
                "ORD-05",
                OrderStatus.DELIVERED,
                new BigDecimal("150000.00")
        );

        createOrder(
                customer,
                "ORD-06",
                OrderStatus.CANCELLED,
                new BigDecimal("160000.00")
        );

        mockMvc.perform(get("/api/reporting/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(1))
                .andExpect(jsonPath("$.confirmed").value(1))
                .andExpect(jsonPath("$.processing").value(1))
                .andExpect(jsonPath("$.readyForDelivery").value(1))
                .andExpect(jsonPath("$.delivered").value(1))
                .andExpect(jsonPath("$.cancelled").value(1))
                .andExpect(jsonPath("$.total").value(6));
    }

    @Test
    void shouldReturnPurchaseSummary() throws Exception {
        Supplier supplier = createSupplier(
                "Reporting Supplier"
        );

        createPurchase(
                supplier,
                "PUR-001",
                "received",
                new BigDecimal("250000.00")
        );

        createPurchase(
                supplier,
                "PUR-002",
                "ordered",
                new BigDecimal("300000.00")
        );

        createPurchase(
                supplier,
                "PUR-003",
                "cancelled",
                new BigDecimal("100000.00")
        );

        mockMvc.perform(get("/api/reporting/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPurchases").value(3))
                .andExpect(jsonPath("$.receivedPurchases").value(1))
                .andExpect(jsonPath("$.totalPurchaseCost").value(250000.00));
    }

    @Test
    void shouldReturnInventorySummary() throws Exception {
        createProduct(
                "Product One",
                "REPORT-001",
                10,
                5
        );

        createProduct(
                "Product Two",
                "REPORT-002",
                3,
                5
        );

        createProduct(
                "Product Three",
                "REPORT-003",
                20,
                5
        );

        mockMvc.perform(get("/api/reporting/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(3))
                .andExpect(jsonPath("$.totalUnitsInStock").value(33))
                .andExpect(jsonPath("$.lowStockProducts").value(1));
    }

    @Test
    void shouldReturnCustomerSummary() throws Exception {
        Customer customerOne = createCustomer(
                "Customer One",
                "0745678901",
                "one@example.com"
        );

        customerOne.setTotalOrders(3);
        customerOne.setTotalSpent(new BigDecimal("450000.00"));
        customerRepository.save(customerOne);

        Customer customerTwo = createCustomer(
                "Customer Two",
                "0756789012",
                "two@example.com"
        );

        customerTwo.setTotalOrders(2);
        customerTwo.setTotalSpent(new BigDecimal("300000.00"));
        customerRepository.save(customerTwo);

        mockMvc.perform(get("/api/reporting/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2))
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.totalSpent").value(750000.00));
    }

    private Customer createCustomer(
            String fullName,
            String phone,
            String email
    ) {
        Customer customer = new Customer();

        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());

        return customerRepository.save(customer);
    }

    private Supplier createSupplier(String name) {
        Supplier supplier = new Supplier();

        supplier.setName(name);
        supplier.setCreatedAt(OffsetDateTime.now());
        supplier.setUpdatedAt(OffsetDateTime.now());

        return supplierRepository.save(supplier);
    }

    private Purchase createPurchase(
            Supplier supplier,
            String purchaseNumber,
            String status,
            BigDecimal totalCost
    ) {
        Purchase purchase = new Purchase();

        purchase.setSupplier(supplier);
        purchase.setPurchaseNumber(purchaseNumber);
        purchase.setStatus(status);
        purchase.setPurchaseDate(
                java.time.LocalDate.of(2026, 9, 10)
        );
        purchase.setTotalCost(totalCost);
        purchase.setCreatedAt(OffsetDateTime.now());
        purchase.setUpdatedAt(OffsetDateTime.now());

        return purchaseRepository.save(purchase);
    }

    private Order createOrder(
            Customer customer,
            String orderNumber,
            OrderStatus status,
            BigDecimal total
    ) {
        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber(orderNumber);
        order.setCustomerName(customer.getFullName());
        order.setCustomerPhone(customer.getPhone());
        order.setCustomerEmail(customer.getEmail());
        order.setDeliveryAddress("Test Address");
        order.setDeliveryCity("Dar es Salaam");
        order.setDeliveryRegion("Dar es Salaam");
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        order.setSubtotal(total);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setTotal(total);
        order.setStatus(status);
        order.setExpiresAt(
                status == OrderStatus.PENDING
                        ? OffsetDateTime.now().plusHours(24)
                        : null
        );
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }

    private Product createProduct(
            String name,
            String sku,
            int stockQuantity,
            int lowStockThreshold
    ) {
        Category category = new Category();

        category.setName("Category-" + sku);
        category.setSlug("category-" + sku.toLowerCase());
        category.setDescription("Reporting test category");
        category.setActive(true);
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());

        category = categoryRepository.save(category);

        Product product = new Product();

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setPrice(new BigDecimal("150000.00"));
        product.setLowStockThreshold(lowStockThreshold);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());

        if (stockQuantity > 0) {
            product.increaseStock(stockQuantity);
        }

        return productRepository.save(product);
    }
}