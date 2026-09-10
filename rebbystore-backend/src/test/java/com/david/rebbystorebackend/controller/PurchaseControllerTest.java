package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseItemRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        purchaseItemRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        supplierRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCreatePurchase() throws Exception {
        Supplier supplier = createSupplier(
                "Beauty Supplier",
                "0712345678"
        );

        String requestBody = """
                {
                  "supplierId": %d,
                  "purchaseDate": "2026-09-10",
                  "notes": "Initial stock order"
                }
                """.formatted(supplier.getId());

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.supplierId").value(supplier.getId()))
                .andExpect(jsonPath("$.supplierName").value("Beauty Supplier"))
                .andExpect(jsonPath("$.purchaseNumber").isString())
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.purchaseDate").value("2026-09-10"))
                .andExpect(jsonPath("$.totalCost").value(0))
                .andExpect(jsonPath("$.notes").value("Initial stock order"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetPurchaseById() throws Exception {
        Supplier supplier = createSupplier(
                "Mambo Supplier",
                "0723456789"
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-TEST01",
                "draft"
        );

        mockMvc.perform(get("/api/purchases/{id}", purchase.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchase.getId()))
                .andExpect(jsonPath("$.supplierId").value(supplier.getId()))
                .andExpect(jsonPath("$.supplierName").value("Mambo Supplier"))
                .andExpect(jsonPath("$.purchaseNumber").value("PUR-TEST01"))
                .andExpect(jsonPath("$.status").value("draft"));
    }

    @Test
    void shouldGetPurchaseByNumber() throws Exception {
        Supplier supplier = createSupplier(
                "Premium Supplier",
                "0734567890"
        );

        createPurchase(
                supplier,
                "PUR-NUMBER1",
                "draft"
        );

        mockMvc.perform(
                        get("/api/purchases/number/{purchaseNumber}",
                                "PUR-NUMBER1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchaseNumber")
                        .value("PUR-NUMBER1"))
                .andExpect(jsonPath("$.supplierName")
                        .value("Premium Supplier"));
    }

    @Test
    void shouldGetPurchasesBySupplier() throws Exception {
        Supplier supplier = createSupplier(
                "Supplier A",
                "0745678901"
        );

        createPurchase(
                supplier,
                "PUR-SUP01",
                "draft"
        );

        createPurchase(
                supplier,
                "PUR-SUP02",
                "ordered"
        );

        mockMvc.perform(
                        get("/api/purchases/supplier/{supplierId}",
                                supplier.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetPurchasesByStatus() throws Exception {
        Supplier supplier = createSupplier(
                "Supplier Status",
                "0756789012"
        );

        createPurchase(
                supplier,
                "PUR-DRAFT01",
                "draft"
        );

        createPurchase(
                supplier,
                "PUR-ORDER01",
                "ordered"
        );

        mockMvc.perform(
                        get("/api/purchases/status/{status}", "ordered")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].purchaseNumber")
                        .value("PUR-ORDER01"))
                .andExpect(jsonPath("$[0].status")
                        .value("ordered"));
    }

    @Test
    void shouldAddPurchaseItemAndCalculateTotal() throws Exception {
        Supplier supplier = createSupplier(
                "Item Supplier",
                "0767890123"
        );

        Product product = createProduct(
                "Body Wave Wig",
                "WIG-001",
                0
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-ITEM01",
                "draft"
        );

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": 3,
                  "unitCost": 50000.00
                }
                """.formatted(product.getId());

        mockMvc.perform(
                        post("/api/purchases/{id}/items", purchase.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.productName")
                        .value("Body Wave Wig"))
                .andExpect(jsonPath("$.sku")
                        .value("WIG-001"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.unitCost").value(50000.00))
                .andExpect(jsonPath("$.subtotal").value(150000.00));

        mockMvc.perform(
                        get("/api/purchases/{id}", purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost")
                        .value(150000.00));
    }

    @Test
    void shouldGetPurchaseItems() throws Exception {
        Supplier supplier = createSupplier(
                "Items Supplier",
                "0778901234"
        );

        Product product = createProduct(
                "Straight Wig",
                "WIG-002",
                0
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-ITEM02",
                "draft"
        );

        addItem(
                purchase.getId(),
                product.getId(),
                2,
                "75000.00"
        );

        mockMvc.perform(
                        get("/api/purchases/{id}/items", purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$[0].subtotal")
                        .value(150000.00));
    }

    @Test
    void shouldRemovePurchaseItem() throws Exception {
        Supplier supplier = createSupplier(
                "Remove Supplier",
                "0711111111"
        );

        Product product = createProduct(
                "Curly Wig",
                "WIG-003",
                0
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-REMOVE1",
                "draft"
        );

        long itemId = addItem(
                purchase.getId(),
                product.getId(),
                2,
                "60000.00"
        );

        mockMvc.perform(
                        delete("/api/purchases/items/{itemId}", itemId)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/purchases/{id}/items", purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldOrderPurchase() throws Exception {
        Supplier supplier = createSupplier(
                "Order Supplier",
                "0722222222"
        );

        Product product = createProduct(
                "Deep Wave Wig",
                "WIG-004",
                0
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-ORDER1",
                "draft"
        );

        addItem(
                purchase.getId(),
                product.getId(),
                2,
                "80000.00"
        );

        mockMvc.perform(
                        post("/api/purchases/{id}/order",
                                purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ordered"))
                .andExpect(jsonPath("$.totalCost")
                        .value(160000.00));
    }

    @Test
    void shouldReceivePurchaseAndIncreaseStock() throws Exception {
        Supplier supplier = createSupplier(
                "Receive Supplier",
                "0733333333"
        );

        Product product = createProduct(
                "Lace Wig",
                "WIG-005",
                5
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-RECEIVE1",
                "draft"
        );

        addItem(
                purchase.getId(),
                product.getId(),
                4,
                "90000.00"
        );

        mockMvc.perform(
                        post("/api/purchases/{id}/order",
                                purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ordered"));

        mockMvc.perform(
                        post("/api/purchases/{id}/receive",
                                purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("received"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                9,
                updatedProduct.getStockQuantity()
        );
    }

    @Test
    void shouldCancelDraftPurchase() throws Exception {
        Supplier supplier = createSupplier(
                "Cancel Supplier",
                "0744444444"
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-CANCEL1",
                "draft"
        );

        mockMvc.perform(
                        post("/api/purchases/{id}/cancel",
                                purchase.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("cancelled"));
    }

    @Test
    void shouldRejectOrderingPurchaseWithoutItems() throws Exception {
        Supplier supplier = createSupplier(
                "Empty Supplier",
                "0755555555"
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-EMPTY01",
                "draft"
        );

        mockMvc.perform(
                        post("/api/purchases/{id}/order",
                                purchase.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Purchase must contain at least one item"));
    }

    @Test
    void shouldRejectAddingItemToOrderedPurchase() throws Exception {
        Supplier supplier = createSupplier(
                "Ordered Supplier",
                "0766666666"
        );

        Product product = createProduct(
                "Test Wig",
                "WIG-006",
                0
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-ORDER02",
                "ordered"
        );

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": 1,
                  "unitCost": 50000.00
                }
                """.formatted(product.getId());

        mockMvc.perform(
                        post("/api/purchases/{id}/items",
                                purchase.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidItemRequest() throws Exception {
        Supplier supplier = createSupplier(
                "Validation Supplier",
                "0777777777"
        );

        Purchase purchase = createPurchase(
                supplier,
                "PUR-VALID01",
                "draft"
        );

        String requestBody = """
                {
                  "productId": null,
                  "quantity": 0,
                  "unitCost": 0
                }
                """;

        mockMvc.perform(
                        post("/api/purchases/{id}/items",
                                purchase.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.productId")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.quantity")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.unitCost")
                        .exists());
    }

    @Test
    void shouldReturnNotFoundForMissingPurchase() throws Exception {
        mockMvc.perform(
                        get("/api/purchases/{id}", 999999L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Purchase with id 999999 not found"));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        String requestBody = """
                {
                  "supplierId": null,
                  "purchaseDate": null,
                  "notes": "Test"
                }
                """;

        mockMvc.perform(
                        post("/api/purchases")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.supplierId")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.purchaseDate")
                        .exists());
    }

    private Supplier createSupplier(
            String name,
            String phone
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    private Purchase createPurchase(
            Supplier supplier,
            String purchaseNumber,
            String status
    ) {
        Purchase purchase = new Purchase();

        purchase.setSupplier(supplier);
        purchase.setPurchaseNumber(purchaseNumber);
        purchase.setStatus(status);
        purchase.setPurchaseDate(LocalDate.of(2026, 9, 10));
        purchase.setTotalCost(BigDecimal.ZERO);
        purchase.setCreatedAt(OffsetDateTime.now());
        purchase.setUpdatedAt(OffsetDateTime.now());

        return purchaseRepository.save(purchase);
    }

    private Product createProduct(
            String name,
            String sku,
            int stockQuantity
    ) {
        Category category = createCategory(
                "Test Category " + sku,
                "test-category-" + sku.toLowerCase()
        );

        Product product = new Product();

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setPrice(BigDecimal.valueOf(150000));
        product.setLowStockThreshold(5);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());

        if (stockQuantity > 0) {
            product.increaseStock(stockQuantity);
        }

        return productRepository.save(product);
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

    private long addItem(
            Long purchaseId,
            Long productId,
            int quantity,
            String unitCost
    ) throws Exception {

        String requestBody = """
                {
                  "productId": %d,
                  "quantity": %d,
                  "unitCost": %s
                }
                """.formatted(
                productId,
                quantity,
                unitCost
        );

        String response = mockMvc.perform(
                        post("/api/purchases/{id}/items", purchaseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
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