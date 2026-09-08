package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
        validateRequiredFields(name, sku, price, lowStockThreshold);

        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException(
                    "Product with SKU '" + sku + "' already exists"
            );
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category with id " + categoryId + " not found"
                        )
                );

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot create a product under an inactive category"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Product product = new Product();
        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setDescription(description);
        product.setPrice(price);
        product.setColor(color);
        product.setTexture(texture);
        product.setLength(length);
        product.setHairType(hairType);

        // Stock is controlled by InventoryService.
        product.setLowStockThreshold(lowStockThreshold);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + id + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with SKU '" + sku + "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

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
        validateRequiredFields(name, sku, price, lowStockThreshold);

        Product product = getProductById(id);

        productRepository.findBySku(sku)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Product with SKU '" + sku + "' already exists"
                    );
                });

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category with id " + categoryId + " not found"
                        )
                );

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot assign a product to an inactive category"
            );
        }

        product.setCategory(category);
        product.setName(name);
        product.setSku(sku);
        product.setDescription(description);
        product.setPrice(price);
        product.setColor(color);
        product.setTexture(texture);
        product.setLength(length);
        product.setHairType(hairType);
        product.setLowStockThreshold(lowStockThreshold);
        product.setUpdatedAt(OffsetDateTime.now());

        return productRepository.save(product);
    }

    public Product deactivateProduct(Long id) {
        Product product = getProductById(id);

        if (product.getStatus() == ProductStatus.INACTIVE) {
            return product;
        }

        product.setStatus(ProductStatus.INACTIVE);
        product.setUpdatedAt(OffsetDateTime.now());

        return productRepository.save(product);
    }

    private void validateRequiredFields(
            String name,
            String sku,
            BigDecimal price,
            Integer lowStockThreshold
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("Product SKU is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        if (lowStockThreshold == null || lowStockThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low stock threshold cannot be negative"
            );
        }
    }
}