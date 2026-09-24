package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.OrderItemRepository;
import com.david.rebbystorebackend.repository.OrderRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD =
            new BigDecimal("200000");

    private static final BigDecimal STANDARD_DELIVERY_FEE =
            new BigDecimal("5000");

    private static final int PENDING_EXPIRY_HOURS = 24;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    /*
     * Creates a new PENDING order.
     *
     * Stock is NOT deducted here.
     */
    public Order createOrder(
            Long customerId,
            String deliveryAddress,
            String deliveryCity,
            String deliveryRegion,
            String deliveryNotes,
            PaymentMethod paymentMethod
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer with id " + customerId + " not found"
                        )
                );

        validateRequired(
                deliveryAddress,
                "Delivery address"
        );

        validateRequired(
                deliveryCity,
                "Delivery city"
        );

        validateRequired(
                deliveryRegion,
                "Delivery region"
        );

        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Payment method must not be null"
            );
        }

        if (paymentMethod != PaymentMethod.CASH_ON_DELIVERY) {
            throw new IllegalArgumentException(
                    "Only cash on delivery is supported"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber(generateOrderNumber());

        /*
         * Customer snapshot.
         *
         * Changes to the customer later should not modify
         * historical order information.
         */
        order.setCustomerName(customer.getFullName());
        order.setCustomerPhone(customer.getPhone());
        order.setCustomerEmail(customer.getEmail());

        order.setDeliveryAddress(deliveryAddress.trim());
        order.setDeliveryCity(deliveryCity.trim());
        order.setDeliveryRegion(deliveryRegion.trim());
        order.setDeliveryNotes(
                normalizeOptional(deliveryNotes)
        );

        order.setPaymentMethod(paymentMethod);

        order.setSubtotal(BigDecimal.ZERO);
        order.setDeliveryFee(STANDARD_DELIVERY_FEE);
        order.setTotal(STANDARD_DELIVERY_FEE);

        order.setStatus(OrderStatus.PENDING);

        order.setExpiresAt(
                now.plusHours(PENDING_EXPIRY_HOURS)
        );

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    /*
     * Adds a product to a pending order.
     *
     * Stock is checked but NOT deducted.
     */
    public OrderItem addItem(
            Long orderId,
            Long productId,
            int quantity
    ) {
        Order order = getById(orderId);

        ensureStatus(
                order,
                OrderStatus.PENDING
        );

        ensureNotExpired(order);

        validateQuantity(quantity);

        Product product = productRepository
                .findByIdForUpdate(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product with id " +
                                        productId +
                                        " not found"
                        )
                );

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Product with id " +
                            productId +
                            " is not active"
            );
        }

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product with SKU '" +
                            product.getSku() +
                            "'"
            );
        }

        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProduct(product);

        /*
         * Product snapshot.
         */
        item.setProductName(product.getName());
        item.setSku(product.getSku());

        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());

        BigDecimal subtotal =
                product.getPrice()
                        .multiply(BigDecimal.valueOf(quantity));

        item.setSubtotal(subtotal);

        OrderItem savedItem =
                orderItemRepository.save(item);

        recalculateTotals(order);

        return savedItem;
    }

    /*
     * Removes an item from a pending order.
     */
    public void removeItem(Long orderItemId) {
        OrderItem item =
                orderItemRepository.findById(orderItemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order item with id " +
                                                orderItemId +
                                                " not found"
                                )
                        );

        Order order = item.getOrder();

        ensureStatus(
                order,
                OrderStatus.PENDING
        );

        ensureNotExpired(order);

        orderItemRepository.delete(item);

        recalculateTotals(order);
    }

    /*
     * Confirms the order.
     *
     * This is the point where stock is deducted.
     */
    public Order confirm(Long orderId) {
        Order order = getById(orderId);

        ensureStatus(
                order,
                OrderStatus.PENDING
        );

        ensureNotExpired(order);

        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }

        /*
         * Recheck every product while holding a write lock.
         *
         * This protects the system against stock changes
         * between adding the item and confirming the order.
         */
        for (OrderItem item : items) {

            Product product =
                    productRepository
                            .findByIdForUpdate(
                                    item.getProduct().getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product with id " +
                                                    item.getProduct().getId() +
                                                    " not found"
                                    )
                            );

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Product with SKU '" +
                                product.getSku() +
                                "' is no longer active"
                );
            }

            if (product.getStockQuantity()
                    < item.getQuantity()) {

                throw new IllegalArgumentException(
                        "Insufficient stock for product with SKU '" +
                                product.getSku() +
                                "'"
                );
            }
        }

        /*
         * Deduct stock and create sale movements.
         *
         * Everything runs inside this transaction.
         */
        for (OrderItem item : items) {

            InventoryMovement movement =
                    inventoryService.deductForSale(
                            item.getProduct().getId(),
                            item.getQuantity(),
                            order
                    );

            /*
             * The variable is intentionally retained here as a clear
             * indication that the inventory operation produced a
             * persisted movement.
             */
            if (movement.getId() == null) {
                throw new IllegalStateException(
                        "Failed to create inventory movement"
                );
            }
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }

    /*
     * CONFIRMED → PROCESSING
     */
    public Order process(Long orderId) {
        Order order = getById(orderId);

        ensureStatus(
                order,
                OrderStatus.CONFIRMED
        );

        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }

    /*
     * PROCESSING → READY_FOR_DELIVERY
     */
    public Order markReadyForDelivery(Long orderId) {
        Order order = getById(orderId);

        ensureStatus(
                order,
                OrderStatus.PROCESSING
        );

        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }

    /*
     * READY_FOR_DELIVERY → DELIVERED
     *
     * Customer statistics are updated here because this is
     * the point at which the order has actually been completed.
     */
    public Order markDelivered(Long orderId) {
        Order order = getById(orderId);

        ensureStatus(
                order,
                OrderStatus.READY_FOR_DELIVERY
        );

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(OffsetDateTime.now());

        Customer customer = order.getCustomer();

        customer.setTotalOrders(
                customer.getTotalOrders() + 1
        );

        customer.setTotalSpent(
                customer.getTotalSpent()
                        .add(order.getTotal())
        );

        customer.setUpdatedAt(OffsetDateTime.now());

        customerRepository.save(customer);

        return orderRepository.save(order);
    }

    /*
     * Cancels a pending or confirmed order.
     *
     * PENDING:
     *     nothing to restore because stock was never deducted.
     *
     * CONFIRMED:
     *     stock must be restored.
     */
    public Order cancel(Long orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "Only pending or confirmed orders can be cancelled"
            );
        }

        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId);

        /*
         * Stock was deducted at confirmation.
         */
        if (order.getStatus() == OrderStatus.CONFIRMED) {

            for (OrderItem item : items) {

                InventoryMovement movement =
                        inventoryService.restoreCancelledOrder(
                                item.getProduct().getId(),
                                item.getQuantity(),
                                order
                        );

                if (movement.getId() == null) {
                    throw new IllegalStateException(
                            "Failed to create inventory restoration movement"
                    );
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setExpiresAt(null);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderRepository.save(order);
    }

    /*
     * Finds and cancels pending orders whose expiry time has passed.
     *
     * Pending orders have not consumed inventory, therefore
     * no stock restoration is required.
     */
    public int expirePendingOrders() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Order> expiredOrders =
                orderRepository
                        .findByStatusAndExpiresAtBefore(
                                OrderStatus.PENDING,
                                now
                        );

        int expiredCount = 0;

        for (Order order : expiredOrders) {

            order.setStatus(OrderStatus.CANCELLED);
            order.setExpiresAt(null);
            order.setUpdatedAt(now);

            orderRepository.save(order);

            expiredCount++;
        }

        return expiredCount;
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order with id " +
                                        id +
                                        " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Order getByOrderNumber(
            String orderNumber
    ) {
        if (orderNumber == null ||
                orderNumber.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Order number must not be blank"
            );
        }

        String normalizedNumber = orderNumber.trim();

        return orderRepository
                .findByOrderNumber(normalizedNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order with number '" +
                                        normalizedNumber +
                                        "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItems(Long orderId) {
        getById(orderId);

        return orderItemRepository
                .findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> getCustomerOrders(
            Long customerId
    ) {
        return orderRepository
                .findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getByStatus(
            OrderStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status must not be null"
            );
        }

        return orderRepository.findByStatus(status);
    }

    /*
     * Recalculates:
     *
     * subtotal = sum(item subtotals)
     *
     * subtotal >= 200,000
     *     → delivery = 0
     *
     * subtotal < 200,000
     *     → delivery = 5,000
     *
     * total = subtotal + delivery
     */
    private void recalculateTotals(Order order) {

        List<OrderItem> items =
                orderItemRepository
                        .findByOrderId(order.getId());

        BigDecimal subtotal =
                items.stream()
                        .map(OrderItem::getSubtotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal deliveryFee =
                subtotal.compareTo(
                        FREE_DELIVERY_THRESHOLD
                ) >= 0
                        ? BigDecimal.ZERO
                        : STANDARD_DELIVERY_FEE;

        BigDecimal total =
                subtotal.add(deliveryFee);

        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(total);
        order.setUpdatedAt(OffsetDateTime.now());

        orderRepository.save(order);
    }

    private String generateOrderNumber() {

        String orderNumber;

        do {
            orderNumber =
                    "ORD-" +
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();

        } while (
                orderRepository
                        .existsByOrderNumber(orderNumber)
        );

        return orderNumber;
    }

    private void validateRequired(
            String value,
            String fieldName
    ) {
        if (value == null ||
                value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero"
            );
        }
    }

    private String normalizeOptional(String value) {
        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private void ensureStatus(
            Order order,
            OrderStatus expected
    ) {
        if (order.getStatus() != expected) {

            throw new IllegalArgumentException(
                    "Order with number '" +
                            order.getOrderNumber() +
                            "' must be in status '" +
                            expected +
                            "'"
            );
        }
    }

    private void ensureNotExpired(Order order) {

        if (order.getExpiresAt() != null &&
                !order.getExpiresAt()
                        .isAfter(OffsetDateTime.now())) {

            /*
             * We deliberately do not change the order here.
             *
             * This method throws inside a transaction, which would
             * roll the transaction back. Actual expiry/cancellation
             * is handled by expirePendingOrders().
             */
            throw new IllegalArgumentException(
                    "Order has expired"
            );
        }
    }
}