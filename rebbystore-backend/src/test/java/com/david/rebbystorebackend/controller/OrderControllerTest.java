package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.OrderItemRepository;
import com.david.rebbystorebackend.repository.OrderRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();

        UserPrincipal staffPrincipal = UserPrincipal.createForTesting(
                2L,
                "staff",
                "staff@rebbystore.co.tz",
                "$2a$10$dummy",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_STAFF"))
        );

        when(jwtService.isTokenValid(STAFF_TOKEN))
                .thenReturn(true);

        when(jwtService.extractUsername(STAFF_TOKEN))
                .thenReturn(staffPrincipal.getUsername());

        when(userDetailsService.loadUserByUsername("staff"))
                .thenReturn(staffPrincipal);
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request
    ) {
        return request.header(
                "Authorization",
                "Bearer " + STAFF_TOKEN
        );
    }

    @Test
    void shouldCreatePendingOrder() throws Exception {
        Customer customer = createCustomer(
                "David Customer",
                "0712345678",
                "david@example.com"
        );

        String requestBody = """
                {
                  "customerId": %d,
                  "deliveryAddress": "12 Mikocheni Road",
                  "deliveryCity": "Dar es Salaam",
                  "deliveryRegion": "Dar es Salaam",
                  "deliveryNotes": "Call before delivery",
                  "paymentMethod": "CASH_ON_DELIVERY"
                }
                """.formatted(customer.getId());

        mockMvc.perform(
                        authenticated(
                                post("/api/orders")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(customer.getId()))
                .andExpect(jsonPath("$.orderNumber").isString())
                .andExpect(jsonPath("$.customerName").value("David Customer"))
                .andExpect(jsonPath("$.customerPhone").value("0712345678"))
                .andExpect(jsonPath("$.customerEmail").value("david@example.com"))
                .andExpect(jsonPath("$.deliveryAddress").value("12 Mikocheni Road"))
                .andExpect(jsonPath("$.deliveryCity").value("Dar es Salaam"))
                .andExpect(jsonPath("$.deliveryRegion").value("Dar es Salaam"))
                .andExpect(jsonPath("$.deliveryNotes").value("Call before delivery"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH_ON_DELIVERY"))
                .andExpect(jsonPath("$.subtotal").value(0))
                .andExpect(jsonPath("$.deliveryFee").value(5000))
                .andExpect(jsonPath("$.total").value(5000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetOrderById() throws Exception {
        Customer customer = createCustomer(
                "Customer One",
                "0723456789",
                "one@example.com"
        );

        Order order = createOrder(
                customer,
                "ORD-TEST01",
                OrderStatus.PENDING
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/orders/{id}", order.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.customerId").value(customer.getId()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-TEST01"))
                .andExpect(jsonPath("$.customerName").value("Customer One"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldGetOrderByNumber() throws Exception {
        Customer customer = createCustomer(
                "Customer Number",
                "0734567890",
                "number@example.com"
        );

        createOrder(
                customer,
                "ORD-NUMBER1",
                OrderStatus.PENDING
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/orders/number/{orderNumber}",
                                        "ORD-NUMBER1"
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber")
                        .value("ORD-NUMBER1"))
                .andExpect(jsonPath("$.customerName")
                        .value("Customer Number"));
    }

    @Test
    void shouldGetCustomerOrders() throws Exception {
        Customer customer = createCustomer(
                "Customer Orders",
                "0745678901",
                "orders@example.com"
        );

        createOrder(
                customer,
                "ORD-CUST01",
                OrderStatus.PENDING
        );

        createOrder(
                customer,
                "ORD-CUST02",
                OrderStatus.CONFIRMED
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/orders/customer/{customerId}",
                                        customer.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetOrdersByStatus() throws Exception {
        Customer customer = createCustomer(
                "Status Customer",
                "0756789012",
                "status@example.com"
        );

        createOrder(
                customer,
                "ORD-STATUS01",
                OrderStatus.PENDING
        );

        createOrder(
                customer,
                "ORD-STATUS02",
                OrderStatus.CONFIRMED
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/orders/status/{status}",
                                        "CONFIRMED"
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNumber")
                        .value("ORD-STATUS02"))
                .andExpect(jsonPath("$[0].status")
                        .value("CONFIRMED"));
    }

    @Test
    void shouldAddOrderItemAndCalculateTotals() throws Exception {
        Customer customer = createCustomer(
                "Item Customer",
                "0767890123",
                "item@example.com"
        );

        Product product = createProduct(
                "Body Wave Wig",
                "WIG-ORDER01",
                10,
                "150000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-ITEM01",
                OrderStatus.PENDING
        );

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": 1
                }
                """.formatted(product.getId());

        mockMvc.perform(
                        authenticated(
                                post("/api/orders/{id}/items", order.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.productName")
                        .value("Body Wave Wig"))
                .andExpect(jsonPath("$.sku")
                        .value("WIG-ORDER01"))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.unitPrice").value(150000.00))
                .andExpect(jsonPath("$.subtotal").value(150000.00));

        mockMvc.perform(
                        authenticated(
                                get("/api/orders/{id}", order.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal")
                        .value(150000.00))
                .andExpect(jsonPath("$.deliveryFee")
                        .value(5000))
                .andExpect(jsonPath("$.total")
                        .value(155000.00));
    }

    @Test
    void shouldApplyFreeDeliveryAtThreshold() throws Exception {
        Customer customer = createCustomer(
                "Threshold Customer",
                "0778901234",
                "threshold@example.com"
        );

        Product product = createProduct(
                "Premium Wig",
                "WIG-THRESHOLD",
                10,
                "200000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-THRESHOLD",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 1);

        mockMvc.perform(
                        authenticated(
                                get("/api/orders/{id}", order.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal")
                        .value(200000.00))
                .andExpect(jsonPath("$.deliveryFee")
                        .value(0))
                .andExpect(jsonPath("$.total")
                        .value(200000.00));
    }

    @Test
    void shouldGetOrderItems() throws Exception {
        Customer customer = createCustomer(
                "Items Customer",
                "0711111111",
                "items@example.com"
        );

        Product product = createProduct(
                "Straight Wig",
                "WIG-ORDER02",
                10,
                "120000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-ITEM02",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 2);

        mockMvc.perform(
                        authenticated(
                                get("/api/orders/{id}/items", order.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$[0].productName")
                        .value("Straight Wig"))
                .andExpect(jsonPath("$[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$[0].unitPrice")
                        .value(120000.00))
                .andExpect(jsonPath("$[0].subtotal")
                        .value(240000.00));
    }

    @Test
    void shouldRemoveOrderItem() throws Exception {
        Customer customer = createCustomer(
                "Remove Customer",
                "0722222222",
                "remove@example.com"
        );

        Product product = createProduct(
                "Curly Wig",
                "WIG-ORDER03",
                10,
                "80000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-REMOVE01",
                OrderStatus.PENDING
        );

        long itemId = addItem(
                order.getId(),
                product.getId(),
                2
        );

        mockMvc.perform(
                        authenticated(
                                delete(
                                        "/api/orders/items/{itemId}",
                                        itemId
                                )
                        )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/orders/{id}/items",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldConfirmOrderAndDeductStock() throws Exception {
        Customer customer = createCustomer(
                "Confirm Customer",
                "0733333333",
                "confirm@example.com"
        );

        Product product = createProduct(
                "Deep Wave Wig",
                "WIG-CONFIRM01",
                10,
                "100000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-CONFIRM1",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 3);

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/confirm",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                7,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldProcessConfirmedOrder() throws Exception {
        Customer customer = createCustomer(
                "Process Customer",
                "0744444444",
                "process@example.com"
        );

        Product product = createProduct(
                "Process Wig",
                "WIG-PROCESS01",
                10,
                "100000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-PROCESS1",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 1);

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/confirm",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/process",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PROCESSING"));
    }

    @Test
    void shouldMarkOrderReadyForDelivery() throws Exception {
        Customer customer = createCustomer(
                "Ready Customer",
                "0755555555",
                "ready@example.com"
        );

        Order order = createOrder(
                customer,
                "ORD-READY01",
                OrderStatus.PROCESSING
        );

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/ready-for-delivery",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("READY_FOR_DELIVERY"));
    }

    @Test
    void shouldMarkOrderDeliveredAndUpdateCustomerStats()
            throws Exception {

        Customer customer = createCustomer(
                "Delivered Customer",
                "0766666666",
                "delivered@example.com"
        );

        Product product = createProduct(
                "Delivered Wig",
                "WIG-DELIVER01",
                10,
                "100000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-DELIVER1",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 2);

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/confirm",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/process",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/ready-for-delivery",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/deliver",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("DELIVERED"));

        Customer updatedCustomer = customerRepository
                .findById(customer.getId())
                .orElseThrow();

        assertEquals(
                1,
                updatedCustomer.getTotalOrders()
        );

        assertEquals(
                new BigDecimal("200000.00"),
                updatedCustomer.getTotalSpent()
        );
    }

    @Test
    void shouldCancelPendingOrderWithoutRestoringStock()
            throws Exception {

        Customer customer = createCustomer(
                "Pending Cancel Customer",
                "0777777777",
                "pendingcancel@example.com"
        );

        Product product = createProduct(
                "Pending Cancel Wig",
                "WIG-CANCEL01",
                10,
                "100000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-CANCEL01",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 3);

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/cancel",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                10,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldCancelConfirmedOrderAndRestoreStock()
            throws Exception {

        Customer customer = createCustomer(
                "Confirmed Cancel Customer",
                "0718888888",
                "confirmedcancel@example.com"
        );

        Product product = createProduct(
                "Confirmed Cancel Wig",
                "WIG-CANCEL02",
                10,
                "90000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-CANCEL02",
                OrderStatus.PENDING
        );

        addItem(order.getId(), product.getId(), 4);

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/confirm",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk());

        Product afterConfirmation = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                6,
                afterConfirmation.getStockQuantity()
        );

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/cancel",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));

        Product afterCancellation = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                10,
                afterCancellation.getStockQuantity()
        );
    }

    @Test
    void shouldRejectConfirmingOrderWithoutItems()
            throws Exception {

        Customer customer = createCustomer(
                "Empty Order Customer",
                "0729999999",
                "empty@example.com"
        );

        Order order = createOrder(
                customer,
                "ORD-EMPTY01",
                OrderStatus.PENDING
        );

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/confirm",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Order must contain at least one item"));
    }

    @Test
    void shouldRejectAddingInactiveProduct() throws Exception {
        Customer customer = createCustomer(
                "Inactive Customer",
                "0738888888",
                "inactive@example.com"
        );

        Product product = createProduct(
                "Inactive Wig",
                "WIG-INACTIVE01",
                10,
                "100000.00"
        );

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);

        Order order = createOrder(
                customer,
                "ORD-INACTIVE1",
                OrderStatus.PENDING
        );

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": 1
                }
                """.formatted(product.getId());

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/items",
                                        order.getId()
                                )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Product with id " +
                                        product.getId() +
                                        " is not active"
                        ));
    }

    @Test
    void shouldRejectInsufficientStock() throws Exception {
        Customer customer = createCustomer(
                "Stock Customer",
                "0748888888",
                "stock@example.com"
        );

        Product product = createProduct(
                "Low Stock Wig",
                "WIG-STOCK01",
                2,
                "100000.00"
        );

        Order order = createOrder(
                customer,
                "ORD-STOCK01",
                OrderStatus.PENDING
        );

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": 3
                }
                """.formatted(product.getId());

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/items",
                                        order.getId()
                                )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Insufficient stock for product with SKU 'WIG-STOCK01'"
                        ));
    }

    @Test
    void shouldRejectInvalidOrderRequest() throws Exception {
        String requestBody = """
                {
                  "customerId": null,
                  "deliveryAddress": "",
                  "deliveryCity": "",
                  "deliveryRegion": "",
                  "deliveryNotes": null,
                  "paymentMethod": null
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/orders")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.customerId")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.deliveryAddress")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.deliveryCity")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.deliveryRegion")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.paymentMethod")
                        .exists());
    }

    @Test
    void shouldRejectInvalidItemRequest() throws Exception {
        Customer customer = createCustomer(
                "Validation Customer",
                "0759999999",
                "validation@example.com"
        );

        Order order = createOrder(
                customer,
                "ORD-VALID01",
                OrderStatus.PENDING
        );

        String requestBody = """
                {
                  "productId": null,
                  "quantity": 0
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/items",
                                        order.getId()
                                )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.productId")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.quantity")
                        .exists());
    }

    @Test
    void shouldRejectInvalidStatusTransition() throws Exception {
        Customer customer = createCustomer(
                "Transition Customer",
                "0769999999",
                "transition@example.com"
        );

        Order order = createOrder(
                customer,
                "ORD-TRANSITION",
                OrderStatus.PENDING
        );

        mockMvc.perform(
                        authenticated(
                                post(
                                        "/api/orders/{id}/process",
                                        order.getId()
                                )
                        )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMissingOrder() throws Exception {
        mockMvc.perform(
                        authenticated(
                                get("/api/orders/{id}", 999999L)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Order with id 999999 not found"));
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

    private Category createCategory(
            String name,
            String slug
    ) {
        Category category = new Category();

        category.setName(name);
        category.setSlug(slug);
        category.setDescription("Test category");
        category.setActive(true);
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());

        return categoryRepository.save(category);
    }

    private Product createProduct(
            String name,
            String sku,
            int stockQuantity,
            String price
    ) {
        Category category = createCategory(
                "Category-" + sku,
                "category-" + sku.toLowerCase()
        );

        Product product = new Product();

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setPrice(new BigDecimal(price));
        product.setLowStockThreshold(5);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());

        if (stockQuantity > 0) {
            product.increaseStock(stockQuantity);
        }

        return productRepository.save(product);
    }

    private Order createOrder(
            Customer customer,
            String orderNumber,
            OrderStatus status
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
        order.setSubtotal(BigDecimal.ZERO);
        order.setDeliveryFee(new BigDecimal("5000"));
        order.setTotal(new BigDecimal("5000"));
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

    private long addItem(
            Long orderId,
            Long productId,
            int quantity
    ) throws Exception {

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": %d
                }
                """.formatted(
                productId,
                quantity
        );

        String response = mockMvc.perform(
                        authenticated(
                                post("/api/orders/{id}/items", orderId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return new tools.jackson.databind.ObjectMapper()
                .readTree(response)
                .get("id")
                .asLong();
    }
}