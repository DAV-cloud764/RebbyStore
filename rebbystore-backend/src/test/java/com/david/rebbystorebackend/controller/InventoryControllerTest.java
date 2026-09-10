package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.InventoryMovementRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldStockInProduct() throws Exception {
        Product product = createProduct(
                "Stock In Wig",
                "INV-001",
                5
        );

        String requestBody = """
                {
                  "quantity": 3
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-in",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.productName")
                        .value("Stock In Wig"))
                .andExpect(jsonPath("$.sku")
                        .value("INV-001"))
                .andExpect(jsonPath("$.type")
                        .value("in"))
                .andExpect(jsonPath("$.quantity")
                        .value(3))
                .andExpect(jsonPath("$.reason")
                        .value("adjustment"))
                .andExpect(jsonPath("$.orderId").doesNotExist())
                .andExpect(jsonPath("$.purchaseId").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists());

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                8,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldStockOutProduct() throws Exception {
        Product product = createProduct(
                "Stock Out Wig",
                "INV-002",
                10
        );

        String requestBody = """
                {
                  "quantity": 4
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-out",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.type")
                        .value("out"))
                .andExpect(jsonPath("$.quantity")
                        .value(4))
                .andExpect(jsonPath("$.reason")
                        .value("adjustment"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                6,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldIncreaseStockUsingAdjustment() throws Exception {
        Product product = createProduct(
                "Increase Adjustment Wig",
                "INV-003",
                5
        );

        String requestBody = """
                {
                  "quantity": 7,
                  "increase": true
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/adjust",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.type")
                        .value("in"))
                .andExpect(jsonPath("$.quantity")
                        .value(7))
                .andExpect(jsonPath("$.reason")
                        .value("adjustment"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                12,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldDecreaseStockUsingAdjustment() throws Exception {
        Product product = createProduct(
                "Decrease Adjustment Wig",
                "INV-004",
                10
        );

        String requestBody = """
                {
                  "quantity": 6,
                  "increase": false
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/adjust",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.type")
                        .value("out"))
                .andExpect(jsonPath("$.quantity")
                        .value(6))
                .andExpect(jsonPath("$.reason")
                        .value("adjustment"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals(
                4,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldGetCurrentStock() throws Exception {
        Product product = createProduct(
                "Current Stock Wig",
                "INV-005",
                15
        );

        mockMvc.perform(
                        get("/api/inventory/{productId}/stock",
                                product.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.stockQuantity")
                        .value(15));
    }

    @Test
    void shouldDetectLowStock() throws Exception {
        Product product = createProduct(
                "Low Stock Wig",
                "INV-006",
                5
        );

        product.setLowStockThreshold(5);
        productRepository.save(product);

        mockMvc.perform(
                        get("/api/inventory/{productId}/low-stock",
                                product.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.lowStock")
                        .value(true));
    }

    @Test
    void shouldDetectProductIsNotLowStock() throws Exception {
        Product product = createProduct(
                "Healthy Stock Wig",
                "INV-007",
                10
        );

        product.setLowStockThreshold(5);
        productRepository.save(product);

        mockMvc.perform(
                        get("/api/inventory/{productId}/low-stock",
                                product.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.lowStock")
                        .value(false));
    }

    @Test
    void shouldGetProductMovements() throws Exception {
        Product product = createProduct(
                "Movement Wig",
                "INV-008",
                10
        );

        stockIn(product.getId(), 3);
        stockOut(product.getId(), 2);

        mockMvc.perform(
                        get("/api/inventory/{productId}/movements",
                                product.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$[0].type")
                        .value("out"))
                .andExpect(jsonPath("$[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$[0].reason")
                        .value("adjustment"))
                .andExpect(jsonPath("$[1].type")
                        .value("in"))
                .andExpect(jsonPath("$[1].quantity")
                        .value(3))
                .andExpect(jsonPath("$[1].reason")
                        .value("adjustment"));
    }

    @Test
    void shouldRejectInvalidStockInRequest() throws Exception {
        Product product = createProduct(
                "Invalid Stock In",
                "INV-009",
                5
        );

        String requestBody = """
                {
                  "quantity": 0
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-in",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.quantity")
                        .exists());
    }

    @Test
    void shouldRejectInvalidStockOutRequest() throws Exception {
        Product product = createProduct(
                "Invalid Stock Out",
                "INV-010",
                5
        );

        String requestBody = """
                {
                  "quantity": -2
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-out",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.quantity")
                        .exists());
    }

    @Test
    void shouldRejectInvalidAdjustmentRequest() throws Exception {
        Product product = createProduct(
                "Invalid Adjustment",
                "INV-011",
                5
        );

        String requestBody = """
                {
                  "quantity": 2,
                  "increase": null
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/adjust",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.increase")
                        .exists());
    }

    @Test
    void shouldRejectStockOutWhenInsufficientStock()
            throws Exception {

        Product product = createProduct(
                "Insufficient Stock Wig",
                "INV-012",
                2
        );

        String requestBody = """
                {
                  "quantity": 5
                }
                """;

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-out",
                                product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Insufficient stock for product with SKU 'INV-012'"));
    }

    @Test
    void shouldReturnBadRequestForMissingProduct() throws Exception {
        mockMvc.perform(
                        get("/api/inventory/{productId}/stock",
                                999999L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Product with id 999999 not found"));
    }

    private Product createProduct(
            String name,
            String sku,
            int stockQuantity
    ) {
        Category category = new Category();

        category.setName("Category-" + sku);
        category.setSlug("category-" + sku.toLowerCase());
        category.setDescription("Test category");
        category.setActive(true);
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());

        category = categoryRepository.save(category);

        Product product = new Product();

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setPrice(new BigDecimal("150000.00"));
        product.setLowStockThreshold(5);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());

        if (stockQuantity > 0) {
            product.increaseStock(stockQuantity);
        }

        return productRepository.save(product);
    }

    private void stockIn(
            Long productId,
            int quantity
    ) throws Exception {

        String requestBody = """
                {
                  "quantity": %d
                }
                """.formatted(quantity);

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-in",
                                productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }

    private void stockOut(
            Long productId,
            int quantity
    ) throws Exception {

        String requestBody = """
                {
                  "quantity": %d
                }
                """.formatted(quantity);

        mockMvc.perform(
                        post("/api/inventory/{productId}/stock-out",
                                productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }
}