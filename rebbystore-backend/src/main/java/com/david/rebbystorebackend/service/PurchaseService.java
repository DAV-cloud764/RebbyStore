package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.domain.entity.PurchaseItem;
import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.dto.purchase.PurchaseItemCreateRequest;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseItemRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Purchase create(Long supplierId, LocalDate purchaseDate, String notes) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier with id " + supplierId + " not found"
                ));

        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date must not be null");
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

    /**
     * Updates purchase header and item lines in one transaction.
     * Received purchases also reconcile stock by the quantity delta for each product.
     */
    public Purchase update(
            Long purchaseId,
            Long supplierId,
            LocalDate purchaseDate,
            String notes,
            List<PurchaseItemCreateRequest> itemRequests
    ) {
        Purchase purchase = getById(purchaseId);
        String status = purchase.getStatus() == null
                ? ""
                : purchase.getStatus().trim().toLowerCase();

        if (CANCELLED.equals(status)) {
            throw new IllegalArgumentException("Cancelled purchases cannot be updated");
        }
        if (!DRAFT.equals(status) && !ORDERED.equals(status) && !RECEIVED.equals(status)) {
            throw new IllegalArgumentException(
                    "Only draft, ordered, or received purchases can be updated"
            );
        }
        if (supplierId == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date is required");
        }
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("A purchase must contain at least one item");
        }

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier with id " + supplierId + " not found"
                ));

        List<PurchaseItem> existingItems = purchaseItemRepository.findByPurchaseId(purchaseId);
        Map<Long, Integer> oldQuantities = new LinkedHashMap<>();
        for (PurchaseItem item : existingItems) {
            Long productId = item.getProduct().getId();
            oldQuantities.merge(productId, item.getQuantity(), Integer::sum);
        }

        // Resolve and validate all incoming items before making stock changes.
        Map<Long, Product> resolvedProducts = new LinkedHashMap<>();
        Map<Long, Integer> newQuantities = new LinkedHashMap<>();
        List<PurchaseItemCreateRequest> validatedItems = new ArrayList<>();

        for (PurchaseItemCreateRequest request : itemRequests) {
            if (request == null || request.productId() == null) {
                throw new IllegalArgumentException("Every purchase item must have a product");
            }
            if (request.quantity() == null) {
                throw new IllegalArgumentException("Purchase quantity is required");
            }
            validateQuantity(request.quantity());
            validateUnitCost(request.unitCost());

            Product product = resolvedProducts.computeIfAbsent(request.productId(), id ->
                    productRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product with id " + id + " not found"
                            ))
            );
            newQuantities.merge(product.getId(), request.quantity(), Integer::sum);
            validatedItems.add(request);
        }

        // A received purchase already contributed stock. Apply only the net quantity
        // difference so edits do not add the full quantity a second time.
        if (RECEIVED.equals(status)) {
            Set<Long> productIds = new LinkedHashSet<>(oldQuantities.keySet());
            productIds.addAll(newQuantities.keySet());

            for (Long productId : productIds) {
                int oldQuantity = oldQuantities.getOrDefault(productId, 0);
                int newQuantity = newQuantities.getOrDefault(productId, 0);
                int difference = newQuantity - oldQuantity;

                if (difference > 0) {
                    inventoryService.stockInFromPurchase(productId, difference, purchase);
                } else if (difference < 0) {
                    inventoryService.stockOutFromPurchase(productId, -difference, purchase);
                }
            }
        }

        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setNotes(normalizeOptional(notes));
        purchase.setUpdatedAt(OffsetDateTime.now());
        purchaseRepository.save(purchase);

        purchaseItemRepository.deleteAll(existingItems);
        purchaseItemRepository.flush();

        for (PurchaseItemCreateRequest request : validatedItems) {
            Product product = resolvedProducts.get(request.productId());
            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchase);
            item.setProduct(product);
            item.setQuantity(request.quantity());
            item.setUnitCost(request.unitCost());
            item.setSubtotal(request.unitCost().multiply(BigDecimal.valueOf(request.quantity())));
            purchaseItemRepository.save(item);
        }

        recalculateTotal(purchase);
        return purchase;
    }

    public PurchaseItem addItem(Long purchaseId, Long productId, int quantity, BigDecimal unitCost) {
        Purchase purchase = getById(purchaseId);
        ensureStatus(purchase, DRAFT);
        validateQuantity(quantity);
        validateUnitCost(unitCost);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product with id " + productId + " not found"
                ));

        PurchaseItem item = new PurchaseItem();
        item.setPurchase(purchase);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitCost(unitCost);
        item.setSubtotal(unitCost.multiply(BigDecimal.valueOf(quantity)));

        PurchaseItem savedItem = purchaseItemRepository.save(item);
        recalculateTotal(purchase);
        return savedItem;
    }

    public void removeItem(Long purchaseItemId) {
        PurchaseItem item = purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase item with id " + purchaseItemId + " not found"
                ));
        Purchase purchase = item.getPurchase();
        ensureStatus(purchase, DRAFT);
        purchaseItemRepository.delete(item);
        recalculateTotal(purchase);
    }

    public Purchase order(Long purchaseId) {
        Purchase purchase = getById(purchaseId);
        ensureStatus(purchase, DRAFT);

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchaseId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Purchase must contain at least one item");
        }

        recalculateTotal(purchase);
        purchase.setStatus(ORDERED);
        purchase.setUpdatedAt(OffsetDateTime.now());
        return purchaseRepository.save(purchase);
    }

    public Purchase receive(Long purchaseId) {
        Purchase purchase = getById(purchaseId);
        ensureStatus(purchase, ORDERED);

        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchaseId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Purchase must contain at least one item");
        }

        for (PurchaseItem item : items) {
            inventoryService.stockInFromPurchase(
                    item.getProduct().getId(), item.getQuantity(), purchase
            );
        }

        purchase.setStatus(RECEIVED);
        purchase.setUpdatedAt(OffsetDateTime.now());
        return purchaseRepository.save(purchase);
    }

    public Purchase cancel(Long purchaseId) {
        Purchase purchase = getById(purchaseId);
        if (!DRAFT.equals(purchase.getStatus()) && !ORDERED.equals(purchase.getStatus())) {
            throw new IllegalArgumentException("Only draft or ordered purchases can be cancelled");
        }
        purchase.setStatus(CANCELLED);
        purchase.setUpdatedAt(OffsetDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Transactional(readOnly = true)
    public Purchase getById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase with id " + id + " not found"
                ));
    }

    @Transactional(readOnly = true)
    public Purchase getByPurchaseNumber(String purchaseNumber) {
        if (purchaseNumber == null || purchaseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Purchase number must not be blank");
        }
        return purchaseRepository.findByPurchaseNumber(purchaseNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase with number '" + purchaseNumber.trim() + "' not found"
                ));
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
            throw new IllegalArgumentException("Purchase status must not be blank");
        }
        return purchaseRepository.findByStatusOrderByPurchaseDateDesc(status.trim().toLowerCase());
    }

    private void recalculateTotal(Purchase purchase) {
        List<PurchaseItem> items = purchaseItemRepository.findByPurchaseId(purchase.getId());
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
                    "Purchase with number '" + purchase.getPurchaseNumber()
                            + "' must be in status '" + expectedStatus + "'"
            );
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be greater than zero");
        }
    }

    private void validateUnitCost(BigDecimal unitCost) {
        if (unitCost == null) {
            throw new IllegalArgumentException("Unit cost must not be null");
        }
        if (unitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit cost must be greater than zero");
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
            purchaseNumber = "PUR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (purchaseRepository.existsByPurchaseNumber(purchaseNumber));
        return purchaseNumber;
    }
}
