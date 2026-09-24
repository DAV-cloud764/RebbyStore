package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.PurchaseItem;
import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseItemRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PurchaseService {

    private static final String DRAFT = "draft";
    private static final String ORDERED = "ordered";
    private static final String RECEIVED = "received";
    private static final String CANCELLED = "cancelled";

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryService inventoryService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public Purchase create(
            Long supplierId,
            LocalDate purchaseDate,
            String notes
    ) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Supplier with id " + supplierId + " not found"
                        )
                );

        if (purchaseDate == null) {
            throw new IllegalArgumentException(
                    "Purchase date must not be null"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setPurchaseNumber(generatePurchaseNumber());
        purchase.setStatus(DRAFT);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setTotalCost(BigDecimal.ZERO);
        purchase.setNotes(normalizeOptional(notes));
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);

        return purchaseRepository.save(purchase);
    }

    public PurchaseItem addItem(
            Long purchaseId,
            Long productId,
            int quantity,
            BigDecimal unitCost
    ) {
        Purchase purchase = getById(purchaseId);
        ensureStatus(purchase, DRAFT);

        validateQuantity(quantity);
        validateUnitCost(unitCost);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " + productId + " not found"
                        )
                );

        PurchaseItem item = new PurchaseItem();

        item.setPurchase(purchase);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitCost(unitCost);

        BigDecimal subtotal =
                unitCost.multiply(BigDecimal.valueOf(quantity));

        item.setSubtotal(subtotal);

        PurchaseItem savedItem = purchaseItemRepository.save(item);

        recalculateTotal(purchase);

        return savedItem;
    }

    public void removeItem(Long purchaseItemId) {
        PurchaseItem item = purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Purchase item with id " +
                                        purchaseItemId +
                                        " not found"
                        )
                );

        Purchase purchase = item.getPurchase();

        ensureStatus(purchase, DRAFT);

        purchaseItemRepository.delete(item);

        recalculateTotal(purchase);
    }

    public Purchase order(Long purchaseId) {
        Purchase purchase = getById(purchaseId);

        ensureStatus(purchase, DRAFT);

        List<PurchaseItem> items =
                purchaseItemRepository.findByPurchaseId(purchaseId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must contain at least one item"
            );
        }

        recalculateTotal(purchase);

        purchase.setStatus(ORDERED);
        purchase.setUpdatedAt(OffsetDateTime.now());

        return purchaseRepository.save(purchase);
    }

    public Purchase receive(Long purchaseId) {
        Purchase purchase = getById(purchaseId);

        ensureStatus(purchase, ORDERED);

        List<PurchaseItem> items =
                purchaseItemRepository.findByPurchaseId(purchaseId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase must contain at least one item"
            );
        }

        for (PurchaseItem item : items) {
            inventoryService.stockInFromPurchase(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    purchase
            );
        }

        purchase.setStatus(RECEIVED);
        purchase.setUpdatedAt(OffsetDateTime.now());

        return purchaseRepository.save(purchase);
    }

    public Purchase cancel(Long purchaseId) {
        Purchase purchase = getById(purchaseId);

        if (!DRAFT.equals(purchase.getStatus())
                && !ORDERED.equals(purchase.getStatus())) {
            throw new IllegalArgumentException(
                    "Only draft or ordered purchases can be cancelled"
            );
        }

        purchase.setStatus(CANCELLED);
        purchase.setUpdatedAt(OffsetDateTime.now());

        return purchaseRepository.save(purchase);
    }

    @Transactional(readOnly = true)
    public Purchase getById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Purchase with id " + id + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Purchase getByPurchaseNumber(String purchaseNumber) {
        if (purchaseNumber == null || purchaseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase number must not be blank"
            );
        }

        return purchaseRepository
                .findByPurchaseNumber(purchaseNumber.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Purchase with number '" +
                                        purchaseNumber.trim() +
                                        "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseItem> getItems(Long purchaseId) {
        getById(purchaseId);

        return purchaseItemRepository.findByPurchaseId(purchaseId);
    }

    @Transactional(readOnly = true)
    public List<Purchase> getBySupplier(Long supplierId) {
        return purchaseRepository.findBySupplierId(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Purchase> getByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Purchase status must not be blank"
            );
        }

        return purchaseRepository.findByStatusOrderByPurchaseDateDesc(
                status.trim().toLowerCase()
        );
    }

    private void recalculateTotal(Purchase purchase) {
        List<PurchaseItem> items =
                purchaseItemRepository.findByPurchaseId(purchase.getId());

        BigDecimal total = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchase.setTotalCost(total);
        purchase.setUpdatedAt(OffsetDateTime.now());

        purchaseRepository.save(purchase);
    }

    private void ensureStatus(Purchase purchase, String expectedStatus) {
        if (!expectedStatus.equalsIgnoreCase(purchase.getStatus())) {
            throw new IllegalArgumentException(
                    "Purchase with number '" +
                            purchase.getPurchaseNumber() +
                            "' must be in status '" +
                            expectedStatus +
                            "'"
            );
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Purchase quantity must be greater than zero"
            );
        }
    }

    private void validateUnitCost(BigDecimal unitCost) {
        if (unitCost == null) {
            throw new IllegalArgumentException(
                    "Unit cost must not be null"
            );
        }

        if (unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Unit cost must be greater than zero"
            );
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String generatePurchaseNumber() {
        String purchaseNumber;

        do {
            purchaseNumber =
                    "PUR-" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();
        } while (
                purchaseRepository.existsByPurchaseNumber(purchaseNumber)
        );

        return purchaseNumber;
    }
}