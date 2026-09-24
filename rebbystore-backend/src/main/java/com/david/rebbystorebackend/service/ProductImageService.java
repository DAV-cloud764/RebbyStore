package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductImage;
import com.david.rebbystorebackend.repository.ProductImageRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository
    ) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    public ProductImage addImage(
            Long productId,
            String imageUrl,
            Integer sortOrder,
            boolean primary
    ) {
        validateImageUrl(imageUrl);
        validateSortOrder(sortOrder);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " not found"
                        )
                );

        if (primary) {
            clearPrimaryImages(productId);
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder);
        image.setPrimary(primary);
        image.setCreatedAt(OffsetDateTime.now());

        return productImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Long productId) {
        ensureProductExists(productId);

        return productImageRepository
                .findByProductIdOrderBySortOrderAsc(productId);
    }

    public ProductImage setPrimaryImage(
            Long productId,
            Long imageId
    ) {
        ensureProductExists(productId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product image with id " + imageId + " not found"
                        )
                );

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Product image does not belong to product with id " + productId
            );
        }

        clearPrimaryImages(productId);

        image.setPrimary(true);

        return productImageRepository.save(image);
    }

    public void deleteImage(
            Long productId,
            Long imageId
    ) {
        ensureProductExists(productId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product image with id " + imageId + " not found"
                        )
                );

        if (!image.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Product image does not belong to product with id " + productId
            );
        }

        boolean wasPrimary = Boolean.TRUE.equals(image.getPrimary());

        productImageRepository.delete(image);

        /*
         * If the deleted image was primary, promote the first
         * remaining image to primary when available.
         */
        if (wasPrimary) {
            List<ProductImage> remainingImages =
                    productImageRepository
                            .findByProductIdOrderBySortOrderAsc(productId);

            if (!remainingImages.isEmpty()) {
                ProductImage newPrimary = remainingImages.get(0);
                newPrimary.setPrimary(true);
                productImageRepository.save(newPrimary);
            }
        }
    }

    private void clearPrimaryImages(Long productId) {
        List<ProductImage> images =
                productImageRepository.findByProductId(productId);

        for (ProductImage image : images) {
            if (Boolean.TRUE.equals(image.getPrimary())) {
                image.setPrimary(false);
                productImageRepository.save(image);
            }
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product with id " + productId + " not found"
            );
        }
    }

    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Image URL is required"
            );
        }
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException(
                    "Sort order cannot be negative"
            );
        }
    }
}