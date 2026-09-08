package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.Purchase;
import com.david.rebbystorebackend.dto.reporting.CustomerSummary;
import com.david.rebbystorebackend.dto.reporting.InventorySummary;
import com.david.rebbystorebackend.dto.reporting.OrderSummary;
import com.david.rebbystorebackend.dto.reporting.PurchaseSummary;
import com.david.rebbystorebackend.dto.reporting.SalesSummary;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.OrderRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import com.david.rebbystorebackend.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final OrderRepository orderRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public ReportingService(
            OrderRepository orderRepository,
            PurchaseRepository purchaseRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository
    ) {
        this.orderRepository = orderRepository;
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public SalesSummary getSalesSummary() {
        List<Order> deliveredOrders =
                orderRepository.findByStatus(OrderStatus.DELIVERED);

        long deliveredCount = deliveredOrders.size();

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue =
                deliveredCount == 0
                        ? BigDecimal.ZERO
                        : totalRevenue.divide(
                        BigDecimal.valueOf(deliveredCount),
                        2,
                        java.math.RoundingMode.HALF_UP
                );

        return new SalesSummary(
                deliveredCount,
                totalRevenue,
                averageOrderValue
        );
    }

    public OrderSummary getOrderSummary() {
        long pending =
                orderRepository.findByStatus(OrderStatus.PENDING).size();

        long confirmed =
                orderRepository.findByStatus(OrderStatus.CONFIRMED).size();

        long processing =
                orderRepository.findByStatus(OrderStatus.PROCESSING).size();

        long readyForDelivery =
                orderRepository
                        .findByStatus(OrderStatus.READY_FOR_DELIVERY)
                        .size();

        long delivered =
                orderRepository.findByStatus(OrderStatus.DELIVERED).size();

        long cancelled =
                orderRepository.findByStatus(OrderStatus.CANCELLED).size();

        long total =
                pending +
                        confirmed +
                        processing +
                        readyForDelivery +
                        delivered +
                        cancelled;

        return new OrderSummary(
                pending,
                confirmed,
                processing,
                readyForDelivery,
                delivered,
                cancelled,
                total
        );
    }

    public PurchaseSummary getPurchaseSummary() {
        List<Purchase> purchases =
                purchaseRepository.findAll();

        long totalPurchases = purchases.size();

        List<Purchase> receivedPurchases =
                purchases.stream()
                        .filter(p ->
                                "received".equalsIgnoreCase(
                                        p.getStatus()
                                )
                        )
                        .toList();

        BigDecimal totalPurchaseCost =
                receivedPurchases.stream()
                        .map(Purchase::getTotalCost)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new PurchaseSummary(
                totalPurchases,
                receivedPurchases.size(),
                totalPurchaseCost
        );
    }

    public InventorySummary getInventorySummary() {
        List<Product> products =
                productRepository.findAll();

        long totalProducts = products.size();

        long totalUnitsInStock = products.stream()
                .map(Product::getStockQuantity)
                .filter(stock -> stock != null)
                .mapToLong(Integer::longValue)
                .sum();

        long lowStockProducts = products.stream()
                .filter(product ->
                        product.getStockQuantity()
                                <= product.getLowStockThreshold()
                )
                .count();

        return new InventorySummary(
                totalProducts,
                totalUnitsInStock,
                lowStockProducts
        );
    }

    public CustomerSummary getCustomerSummary() {
        List<Customer> customers =
                customerRepository.findAll();

        long totalCustomers = customers.size();

        long totalOrders = customers.stream()
                .map(Customer::getTotalOrders)
                .filter(total -> total != null)
                .mapToLong(Integer::longValue)
                .sum();

        BigDecimal totalSpent = customers.stream()
                .map(Customer::getTotalSpent)
                .filter(total -> total != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CustomerSummary(
                totalCustomers,
                totalOrders,
                totalSpent
        );
    }
}