package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.exception.ConflictException;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // =========================================================
    // GET ACTIVE PRODUCTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE);
    }

    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Product ID is required");
        }

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with ID '" + id + "' not found"
                        )
                );
    }

    // =========================================================
    // GET PRODUCT BY SKU
    // =========================================================

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }

        String normalizedSku = sku.trim();

        return productRepository.findBySku(normalizedSku)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with SKU '" + normalizedSku + "' not found"
                        )
                );
    }

    // =========================================================
    // GET PRODUCTS BY CATEGORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {

        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required");
        }

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException(
                    "Category with ID '" + categoryId + "' not found"
            );
        }

        return productRepository.findByCategoryId(categoryId);
    }

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    public Product createProduct(
            Long categoryId,
            String name,
            String sku,
            String description,
            BigDecimal price,
            String color,
            String texture,
            String length,
            String hairType,
            Integer lowStockThreshold
    ) {

        validateProductData(
                categoryId,
                name,
                sku,
                price,
                lowStockThreshold
        );

        String normalizedName = name.trim();
        String normalizedSku = sku.trim();

        if (productRepository.existsBySku(normalizedSku)) {
            throw new ConflictException(
                    "Product with SKU '" + normalizedSku + "' already exists"
            );
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with ID '" + categoryId + "' not found"
                        )
                );

        if (!category.getActive()) {
            throw new IllegalArgumentException(
                    "Cannot create product under an inactive category"
            );
        }

        Product product = new Product();
        OffsetDateTime now = OffsetDateTime.now();

        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        product.setCategory(category);
        product.setName(normalizedName);
        product.setSku(normalizedSku);
        product.setDescription(description);
        product.setPrice(price);
        product.setColor(color);
        product.setTexture(texture);
        product.setLength(length);
        product.setHairType(hairType);

        product.setUpdatedAt(OffsetDateTime.now());

        /*
         * Stock is deliberately NOT set here.
         *
         * Product stock is controlled by InventoryService through
         * increaseStock() and decreaseStock().
         *
         * The Product entity should initialize stockQuantity to 0.
         */
        product.setLowStockThreshold(lowStockThreshold);
        product.setStatus(ProductStatus.ACTIVE);

        return productRepository.save(product);
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    public Product updateProduct(
            Long id,
            Long categoryId,
            String name,
            String sku,
            String description,
            BigDecimal price,
            String color,
            String texture,
            String length,
            String hairType,
            Integer lowStockThreshold
    ) {

        validateProductData(
                categoryId,
                name,
                sku,
                price,
                lowStockThreshold
        );

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with ID '" + id + "' not found"
                        )
                );

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with ID '" + categoryId + "' not found"
                        )
                );

        if (!category.getActive()) {
            throw new IllegalArgumentException(
                    "Cannot assign product to an inactive category"
            );
        }

        String normalizedName = name.trim();
        String normalizedSku = sku.trim();

        /*
         * The product may keep its current SKU.
         *
         * But if the SKU is being changed, the new SKU must not
         * already belong to another product.
         */
        if (!product.getSku().equalsIgnoreCase(normalizedSku)
                && productRepository.existsBySku(normalizedSku)) {

            throw new ConflictException(
                    "Product with SKU '" + normalizedSku + "' already exists"
            );
        }

        product.setCategory(category);
        product.setName(normalizedName);
        product.setSku(normalizedSku);
        product.setDescription(description);
        product.setPrice(price);
        product.setColor(color);
        product.setTexture(texture);
        product.setLength(length);
        product.setHairType(hairType);
        product.setLowStockThreshold(lowStockThreshold);

        return productRepository.save(product);
    }

    // =========================================================
    // DEACTIVATE PRODUCT
    // =========================================================

    public void deactivateProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with ID '" + id + "' not found"
                        )
                );

        /*
         * Deactivating an already inactive product is intentionally
         * idempotent: the operation simply does nothing.
         */
        if (product.getStatus() == ProductStatus.INACTIVE) {
            return;
        }

        product.setStatus(ProductStatus.INACTIVE);

        productRepository.save(product);
    }

    // =========================================================
    // ACTIVATE PRODUCT
    // =========================================================

    public Product activateProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product with ID '" + id + "' not found"
                        )
                );

        Category category = product.getCategory();

        if (category == null) {
            throw new IllegalArgumentException(
                    "Product must belong to a category"
            );
        }

        if (!category.getActive()) {
            throw new IllegalArgumentException(
                    "Cannot activate a product under an inactive category"
            );
        }

        product.setStatus(ProductStatus.ACTIVE);

        return productRepository.save(product);
    }

    // =========================================================
    // VALIDATE PRODUCT DATA
    // =========================================================

    private void validateProductData(
            Long categoryId,
            String name,
            String sku,
            BigDecimal price,
            Integer lowStockThreshold
    ) {

        if (categoryId == null) {
            throw new IllegalArgumentException(
                    "Category ID is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException(
                    "SKU is required"
            );
        }

        if (price == null) {
            throw new IllegalArgumentException(
                    "Price is required"
            );
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }

        if (lowStockThreshold == null) {
            throw new IllegalArgumentException(
                    "Low stock threshold is required"
            );
        }

        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low stock threshold must not be negative"
            );
        }
    }
}