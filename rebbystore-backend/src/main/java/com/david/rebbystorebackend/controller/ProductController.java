package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.dto.product.ProductCreateRequest;
import com.david.rebbystorebackend.dto.product.ProductResponse;
import com.david.rebbystorebackend.dto.product.ProductUpdateRequest;
import com.david.rebbystorebackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /*
     * GET /api/products
     *
     * Returns active products for the customer-facing shop.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        List<ProductResponse> products =
                productService.getActiveProducts()
                        .stream()
                        .map(ProductResponse::from)
                        .toList();

        return ResponseEntity.ok(products);
    }

    /*
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {
        Product product =
                productService.getProductById(id);

        return ResponseEntity.ok(
                ProductResponse.from(product)
        );
    }

    /*
     * GET /api/products/sku/{sku}
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @PathVariable String sku
    ) {
        Product product =
                productService.getProductBySku(sku);

        return ResponseEntity.ok(
                ProductResponse.from(product)
        );
    }

    /*
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId
    ) {
        List<ProductResponse> products =
                productService.getProductsByCategory(categoryId)
                        .stream()
                        .map(ProductResponse::from)
                        .toList();

        return ResponseEntity.ok(products);
    }

    /*
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        Product product =
                productService.createProduct(
                        request.categoryId(),
                        request.name(),
                        request.sku(),
                        request.description(),
                        request.price(),
                        request.color(),
                        request.texture(),
                        request.length(),
                        request.hairType(),
                        request.lowStockThreshold()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponse.from(product));
    }

    /*
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product product =
                productService.updateProduct(
                        id,
                        request.categoryId(),
                        request.name(),
                        request.sku(),
                        request.description(),
                        request.price(),
                        request.color(),
                        request.texture(),
                        request.length(),
                        request.hairType(),
                        request.lowStockThreshold()
                );

        return ResponseEntity.ok(
                ProductResponse.from(product)
        );
    }

    /*
     * DELETE /api/products/{id}
     *
     * This is a soft delete.
     * ProductService marks the product INACTIVE.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(
            @PathVariable Long id
    ) {
        productService.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}