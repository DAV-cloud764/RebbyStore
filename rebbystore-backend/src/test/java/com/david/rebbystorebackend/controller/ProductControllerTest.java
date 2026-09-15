package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.jwt.JwtService;
import com.david.rebbystorebackend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = createCategory();

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
    void shouldCreateProduct() throws Exception {
        String requestBody = """
                {
                  "name": "Brazilian Body Wave",
                  "sku": "WIG-001",
                  "description": "Premium Brazilian body wave wig",
                  "price": 300000,
                  "categoryId": %d,
                  "color": "Natural Black",
                  "texture": "Body Wave",
                  "length": "20 inches",
                  "hairType": "Brazilian",
                  "lowStockThreshold": 2
                }
                """.formatted(category.getId());

        mockMvc.perform(
                        authenticated(
                                post("/api/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Brazilian Body Wave"))
                .andExpect(jsonPath("$.sku").value("WIG-001"))
                .andExpect(jsonPath("$.price").value(300000))
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.lowStockThreshold").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetActiveProducts() throws Exception {
        Product activeProduct = createProduct(
                "Active Wig",
                "WIG-ACTIVE",
                ProductStatus.ACTIVE
        );

        createProduct(
                "Inactive Wig",
                "WIG-INACTIVE",
                ProductStatus.INACTIVE
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/products")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.id == %d)]"
                                .formatted(activeProduct.getId())
                ).exists());
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product product = createProduct(
                "Body Wave Wig",
                "WIG-002",
                ProductStatus.ACTIVE
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/products/{id}", product.getId())
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Body Wave Wig"))
                .andExpect(jsonPath("$.sku").value("WIG-002"))
                .andExpect(jsonPath("$.categoryId").value(category.getId()));
    }

    @Test
    void shouldGetProductBySku() throws Exception {
        createProduct(
                "Straight Wig",
                "WIG-003",
                ProductStatus.ACTIVE
        );

        mockMvc.perform(
                        authenticated(
                                get("/api/products/sku/{sku}", "WIG-003")
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Straight Wig"))
                .andExpect(jsonPath("$.sku").value("WIG-003"))
                .andExpect(jsonPath("$.categoryId").value(category.getId()));
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {
        createProduct(
                "Category Wig One",
                "WIG-004",
                ProductStatus.ACTIVE
        );

        createProduct(
                "Category Wig Two",
                "WIG-005",
                ProductStatus.ACTIVE
        );

        mockMvc.perform(
                        authenticated(
                                get(
                                        "/api/products/category/{categoryId}",
                                        category.getId()
                                )
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product product = createProduct(
                "Original Wig",
                "WIG-006",
                ProductStatus.ACTIVE
        );

        String requestBody = """
                {
                  "name": "Updated Wig",
                  "sku": "WIG-006-UPDATED",
                  "description": "Updated product description",
                  "price": 450000,
                  "categoryId": %d,
                  "color": "Jet Black",
                  "texture": "Straight",
                  "length": "24 inches",
                  "hairType": "Peruvian",
                  "lowStockThreshold": 5
                }
                """.formatted(category.getId());

        mockMvc.perform(
                        authenticated(
                                put("/api/products/{id}", product.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Updated Wig"))
                .andExpect(jsonPath("$.sku").value("WIG-006-UPDATED"))
                .andExpect(jsonPath("$.price").value(450000))
                .andExpect(jsonPath("$.color").value("Jet Black"))
                .andExpect(jsonPath("$.texture").value("Straight"))
                .andExpect(jsonPath("$.length").value("24 inches"))
                .andExpect(jsonPath("$.hairType").value("Peruvian"))
                .andExpect(jsonPath("$.lowStockThreshold").value(5));
    }

    @Test
    void shouldDeactivateProduct() throws Exception {
        Product product = createProduct(
                "Product To Deactivate",
                "WIG-007",
                ProductStatus.ACTIVE
        );

        mockMvc.perform(
                        authenticated(
                                delete("/api/products/{id}", product.getId())
                        )
                )
                .andExpect(status().isNoContent());

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                ProductStatus.INACTIVE,
                updatedProduct.getStatus()
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        String requestBody = """
                {
                  "name": "",
                  "sku": "",
                  "price": 0,
                  "categoryId": null,
                  "lowStockThreshold": -1
                }
                """;

        mockMvc.perform(
                        authenticated(
                                post("/api/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.sku").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.categoryId").exists())
                .andExpect(jsonPath("$.fieldErrors.lowStockThreshold")
                        .exists());
    }

    @Test
    void shouldReturnConflictForDuplicateSku() throws Exception {
        createProduct(
                "Existing Wig",
                "WIG-001",
                ProductStatus.ACTIVE
        );

        String requestBody = """
                {
                  "name": "Another Wig",
                  "sku": "WIG-001",
                  "description": "Another product",
                  "price": 300000,
                  "categoryId": %d,
                  "lowStockThreshold": 2
                }
                """.formatted(category.getId());

        mockMvc.perform(
                        authenticated(
                                post("/api/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Product with SKU 'WIG-001' already exists"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() throws Exception {
        mockMvc.perform(
                        authenticated(
                                get("/api/products/{id}", 999999L)
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Product with ID '999999' not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/products/999999"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private Category createCategory() {
        Category category = new Category();

        category.setName("Wigs");
        category.setSlug("wigs");
        category.setDescription("Wig products");
        category.setActive(true);

        OffsetDateTime now = OffsetDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    private Product createProduct(
            String name,
            String sku,
            ProductStatus status
    ) {
        Product product = new Product();

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("300000"));
        product.setColor("Black");
        product.setTexture("Body Wave");
        product.setLength("20 inches");
        product.setHairType("Brazilian");
        product.setLowStockThreshold(5);
        product.setStatus(status);

        OffsetDateTime now = OffsetDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }
}