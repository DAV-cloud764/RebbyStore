package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.domain.entity.OrderStatus;
import com.david.rebbystorebackend.dto.order.OrderCreateRequest;
import com.david.rebbystorebackend.dto.order.OrderItemCreateRequest;
import com.david.rebbystorebackend.dto.order.OrderItemResponse;
import com.david.rebbystorebackend.dto.order.OrderResponse;
import com.david.rebbystorebackend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.david.rebbystorebackend.security.UserPrincipal;
import com.david.rebbystorebackend.security.authz.OrderAuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderAuthorizationService orderAuthorizationService;

    public OrderController(
            OrderService orderService,
            OrderAuthorizationService orderAuthorizationService
    ) {
        this.orderService = orderService;
        this.orderAuthorizationService = orderAuthorizationService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        orderAuthorizationService.assertCanAccessCustomer(
                principal,
                request.customerId()
        );

        Order order = orderService.createOrder(
                request.customerId(),
                request.deliveryAddress(),
                request.deliveryCity(),
                request.deliveryRegion(),
                request.deliveryNotes(),
                request.paymentMethod()
        );

        return ResponseEntity
                .created(URI.create("/api/orders/" + order.getId()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Order order = orderService.getById(id);

        orderAuthorizationService.assertCanAccessOrder(
                principal,
                order
        );

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String orderNumber
    ) {
        Order order = orderService.getByOrderNumber(orderNumber);

        orderAuthorizationService.assertCanAccessOrder(
                principal,
                order
        );

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long customerId
    ) {
        orderAuthorizationService.assertCanAccessCustomer(
                principal,
                customerId
        );

        List<OrderResponse> response = orderService
                .getCustomerOrders(customerId)
                .stream()
                .map(OrderResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status
    ) {
        List<OrderResponse> response = orderService
                .getByStatus(status)
                .stream()
                .map(OrderResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeOrderItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId
    ) {
        orderAuthorizationService.assertCanAccessOrderItem(
                principal,
                itemId
        );

        orderService.removeItem(itemId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Order order = orderService.getById(id);

        orderAuthorizationService.assertCanAccessOrder(
                principal,
                order
        );

        List<OrderItemResponse> response = orderService
                .getItems(id)
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderItemResponse> addOrderItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody OrderItemCreateRequest request
    ) {
        Order order = orderService.getById(id);

        orderAuthorizationService.assertCanAccessOrder(
                principal,
                order
        );

        OrderItem item = orderService.addItem(
                id,
                request.productId(),
                request.quantity()
        );

        return ResponseEntity.ok(
                OrderItemResponse.from(item)
        );
    }


    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(
            @PathVariable Long id
    ) {
        Order order = orderService.confirm(id);

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<OrderResponse> processOrder(
            @PathVariable Long id
    ) {
        Order order = orderService.process(id);

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @PostMapping("/{id}/ready-for-delivery")
    public ResponseEntity<OrderResponse> markReadyForDelivery(
            @PathVariable Long id
    ) {
        Order order = orderService.markReadyForDelivery(id);

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> markDelivered(
            @PathVariable Long id
    ) {
        Order order = orderService.markDelivered(id);

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Order order = orderService.getById(id);

        orderAuthorizationService.assertCanAccessOrder(
                principal,
                order
        );

        order = orderService.cancel(id);

        return ResponseEntity.ok(
                OrderResponse.from(order)
        );
    }
}