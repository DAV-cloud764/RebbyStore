package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.ProductImage;
import com.david.rebbystorebackend.service.ProductImageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(
            ProductImageService productImageService
    ) {
        this.productImageService = productImageService;
    }

    /*
     * Public product images.
     *
     * The shop page must be able to load these without login.
     */
    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getProductImages(
            @PathVariable Long productId
    ) {
        List<ProductImageResponse> response =
                productImageService.getProductImages(productId)
                        .stream()
                        .map(ProductImageResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    /*
     * ADMIN / STAFF can add an image.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequest request
    ) {
        ProductImage image = productImageService.addImage(
                productId,
                request.imageUrl(),
                request.sortOrder(),
                request.primary()
        );

        ProductImageResponse response =
                ProductImageResponse.from(image);

        URI location = URI.create(
                "/api/products/" +
                        productId +
                        "/images/" +
                        image.getId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /*
     * ADMIN / STAFF can make an image the primary image.
     */
    @PutMapping("/{imageId}/primary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        ProductImage image =
                productImageService.setPrimaryImage(
                        productId,
                        imageId
                );

        return ResponseEntity.ok(
                ProductImageResponse.from(image)
        );
    }

    /*
     * ADMIN / STAFF can delete an image.
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        productImageService.deleteImage(
                productId,
                imageId
        );

        return ResponseEntity.noContent().build();
    }

    public record ProductImageRequest(
            @NotBlank
            @Size(max = 1000)
            String imageUrl,

            @Min(0)
            int sortOrder,

            boolean primary
    ) {}

    public record ProductImageResponse(
            Long id,
            String imageUrl,
            int sortOrder,
            boolean primary
    ) {
        public static ProductImageResponse from(
                ProductImage image
        ) {
            return new ProductImageResponse(
                    image.getId(),
                    image.getImageUrl(),
                    image.getSortOrder(),
                    image.getPrimary()
            );
        }
    }
}