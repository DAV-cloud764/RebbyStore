package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.repository.InventoryMovementRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryService(
            ProductRepository productRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    /*
     * Manual stock-in.
     */
    public InventoryMovement stockIn(
            Long productId,
            int quantity
    ) {
        validateQuantity(quantity);

        Product product = getProductForUpdate(productId);

        product.increaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "in",
                quantity,
                "adjustment",
                null,
                null
        );
    }

    /*
     * Manual stock-out.
     */
    public InventoryMovement stockOut(
            Long productId,
            int quantity
    ) {
        validateQuantity(quantity);

        Product product = getProductForUpdate(productId);

        product.decreaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "out",
                quantity,
                "adjustment",
                null,
                null
        );
    }

    /*
     * Manual inventory adjustment.
     */
    public InventoryMovement adjustStock(
            Long productId,
            int quantity,
            boolean increase
    ) {
        validateQuantity(quantity);

        Product product = getProductForUpdate(productId);

        if (increase) {
            product.increaseStock(quantity);
            productRepository.save(product);

            return createMovement(
                    product,
                    "in",
                    quantity,
                    "adjustment",
                    null,
                    null
            );
        }

        product.decreaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "out",
                quantity,
                "adjustment",
                null,
                null
        );
    }

    /*
     * Stock-in caused by receiving a purchase.
     *
     * The movement is linked to the purchase.
     */
    public InventoryMovement stockInFromPurchase(
            Long productId,
            int quantity,
            Purchase purchase
    ) {
        validateQuantity(quantity);

        if (purchase == null) {
            throw new IllegalArgumentException(
                    "Purchase must not be null"
            );
        }

        Product product = getProductForUpdate(productId);

        product.increaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "in",
                quantity,
                "purchase",
                null,
                purchase
        );
    }

    /*
     * Stock deduction caused by a confirmed sale.
     *
     * The movement is linked to the order.
     */
    public InventoryMovement deductForSale(
            Long productId,
            int quantity,
            Order order
    ) {
        validateQuantity(quantity);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order must not be null"
            );
        }

        Product product = getProductForUpdate(productId);

        product.decreaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "out",
                quantity,
                "sale",
                order,
                null
        );
    }

    /*
     * Stock restoration caused by cancelling a confirmed order.
     *
     * V11 allows this movement to be:
     *
     * type   = in
     * reason = adjustment
     * order  = cancelled order
     */
    public InventoryMovement restoreCancelledOrder(
            Long productId,
            int quantity,
            Order order
    ) {
        validateQuantity(quantity);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order must not be null"
            );
        }

        Product product = getProductForUpdate(productId);

        product.increaseStock(quantity);
        productRepository.save(product);

        return createMovement(
                product,
                "in",
                quantity,
                "adjustment",
                order,
                null
        );
    }

    @Transactional(readOnly = true)
    public int getCurrentStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " not found"
                        )
                );

        return product.getStockQuantity();
    }

    @Transactional(readOnly = true)
    public boolean isLowStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " not found"
                        )
                );

        return product.getStockQuantity()
                <= product.getLowStockThreshold();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> getProductMovements(
            Long productId
    ) {
        ensureProductExists(productId);

        return inventoryMovementRepository
                .findByProductIdOrderByCreatedAtDesc(productId);
    }

    private Product getProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " not found"
                        )
                );
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product with id " + productId + " not found"
            );
        }
    }

    private InventoryMovement createMovement(
            Product product,
            String type,
            int quantity,
            String reason,
            Order order,
            Purchase purchase
    ) {
        InventoryMovement movement = new InventoryMovement();

        movement.setProduct(product);
        movement.setOrder(order);
        movement.setPurchase(purchase);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setCreatedAt(OffsetDateTime.now());

        return inventoryMovementRepository.save(movement);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Inventory quantity must be greater than zero"
            );
        }
    }
}