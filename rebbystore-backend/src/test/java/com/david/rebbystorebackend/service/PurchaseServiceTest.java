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
class PurchaseServiceTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementRepository.deleteAll();
        purchaseItemRepository.deleteAll();
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        supplierRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Supplier createSupplier() {
        Supplier supplier = new Supplier();

        OffsetDateTime now = OffsetDateTime.now();

        supplier.setName("Test Supplier");
        supplier.setPhone("0712345678");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress("Dar es Salaam");
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    private Product createProduct(String sku) {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName("Human Hair");
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
        product.setPrice(new BigDecimal("300000"));
        product.setColor("Black");
        product.setTexture("Straight");
        product.setLength("16 inches");
        product.setHairType("Human Hair");
        product.setLowStockThreshold(2);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    @Test
    void shouldCreateDraftPurchase() {
        Supplier supplier = createSupplier();

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                "Initial stock purchase"
        );

        assertThat(purchase.getId()).isNotNull();
        assertThat(purchase.getPurchaseNumber())
                .startsWith("PUR-");
        assertThat(purchase.getStatus())
                .isEqualTo("draft");
        assertThat(purchase.getTotalCost())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldAddPurchaseItemAndCalculateSubtotal() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        PurchaseItem item = purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                5,
                new BigDecimal("100000")
        );

        assertThat(item.getSubtotal())
                .isEqualByComparingTo("500000");

        Purchase updated =
                purchaseService.getById(purchase.getId());

        assertThat(updated.getTotalCost())
                .isEqualByComparingTo("500000");
    }

    @Test
    void shouldOrderPurchase() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                3,
                new BigDecimal("120000")
        );

        Purchase ordered =
                purchaseService.order(purchase.getId());

        assertThat(ordered.getStatus())
                .isEqualTo("ordered");
    }

    @Test
    void shouldNotAllowOrderingEmptyPurchase() {
        Supplier supplier = createSupplier();

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        assertThatThrownBy(() ->
                purchaseService.order(purchase.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "at least one item"
                );
    }

    @Test
    void shouldReceivePurchaseAndIncreaseStock() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                5,
                new BigDecimal("100000")
        );

        purchaseService.order(purchase.getId());

        Purchase received =
                purchaseService.receive(purchase.getId());

        assertThat(received.getStatus())
                .isEqualTo("received");

        Product updatedProduct =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(updatedProduct.getStockQuantity())
                .isEqualTo(5);

        List<InventoryMovement> movements =
                inventoryMovementRepository
                        .findByPurchaseId(purchase.getId());

        assertThat(movements)
                .hasSize(1);

        assertThat(movements.get(0).getType())
                .isEqualTo("in");

        assertThat(movements.get(0).getReason())
                .isEqualTo("purchase");

        assertThat(movements.get(0).getPurchase().getId())
                .isEqualTo(purchase.getId());
    }

    @Test
    void shouldNotIncreaseStockWhenPurchaseIsOnlyCreated() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                5,
                new BigDecimal("100000")
        );

        Product unchanged =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(unchanged.getStockQuantity())
                .isZero();
    }

    @Test
    void shouldNotReceiveDraftPurchase() {
        Supplier supplier = createSupplier();

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        assertThatThrownBy(() ->
                purchaseService.receive(purchase.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status 'ordered'");
    }

    @Test
    void shouldNotReceivePurchaseTwice() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                4,
                new BigDecimal("100000")
        );

        purchaseService.order(purchase.getId());
        purchaseService.receive(purchase.getId());

        assertThatThrownBy(() ->
                purchaseService.receive(purchase.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "status 'ordered'"
                );
    }

    @Test
    void shouldCancelDraftPurchase() {
        Supplier supplier = createSupplier();

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        Purchase cancelled =
                purchaseService.cancel(purchase.getId());

        assertThat(cancelled.getStatus())
                .isEqualTo("cancelled");
    }

    @Test
    void shouldCancelOrderedPurchase() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                2,
                new BigDecimal("100000")
        );

        purchaseService.order(purchase.getId());

        Purchase cancelled =
                purchaseService.cancel(purchase.getId());

        assertThat(cancelled.getStatus())
                .isEqualTo("cancelled");
    }

    @Test
    void shouldNotCancelReceivedPurchase() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                2,
                new BigDecimal("100000")
        );

        purchaseService.order(purchase.getId());
        purchaseService.receive(purchase.getId());

        assertThatThrownBy(() ->
                purchaseService.cancel(purchase.getId())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "draft or ordered"
                );
    }

    @Test
    void shouldRemovePurchaseItemAndRecalculateTotal() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        PurchaseItem item = purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                5,
                new BigDecimal("100000")
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                2,
                new BigDecimal("50000")
        );

        assertThat(
                purchaseService.getById(purchase.getId()).getTotalCost()
        ).isEqualByComparingTo("600000");

        purchaseService.removeItem(item.getId());

        Purchase updated =
                purchaseService.getById(purchase.getId());

        assertThat(updated.getTotalCost())
                .isEqualByComparingTo("100000");
    }

    @Test
    void shouldReturnPurchaseItems() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        purchaseService.addItem(
                purchase.getId(),
                product.getId(),
                3,
                new BigDecimal("100000")
        );

        List<PurchaseItem> items =
                purchaseService.getItems(purchase.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity())
                .isEqualTo(3);
    }

    @Test
    void shouldRejectInvalidQuantity() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        assertThatThrownBy(() ->
                purchaseService.addItem(
                        purchase.getId(),
                        product.getId(),
                        0,
                        new BigDecimal("100000")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "quantity must be greater than zero"
                );
    }

    @Test
    void shouldRejectInvalidUnitCost() {
        Supplier supplier = createSupplier();
        Product product = createProduct("WIG-001");

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        assertThatThrownBy(() ->
                purchaseService.addItem(
                        purchase.getId(),
                        product.getId(),
                        2,
                        BigDecimal.ZERO
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Unit cost must be greater than zero"
                );
    }

    @Test
    void shouldRejectNonExistingSupplier() {
        assertThatThrownBy(() ->
                purchaseService.create(
                        999999L,
                        LocalDate.now(),
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Supplier with id 999999 not found"
                );
    }

    @Test
    void shouldRejectNonExistingProduct() {
        Supplier supplier = createSupplier();

        Purchase purchase = purchaseService.create(
                supplier.getId(),
                LocalDate.now(),
                null
        );

        assertThatThrownBy(() ->
                purchaseService.addItem(
                        purchase.getId(),
                        999999L,
                        2,
                        new BigDecimal("100000")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Product with id 999999 not found"
                );
    }
}