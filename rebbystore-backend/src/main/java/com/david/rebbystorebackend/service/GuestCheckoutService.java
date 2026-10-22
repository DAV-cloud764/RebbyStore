package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.domain.entity.PaymentMethod;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.dto.checkout.GuestCheckoutRequest;
import com.david.rebbystorebackend.dto.checkout.GuestCheckoutResponse;
import com.david.rebbystorebackend.dto.checkout.GuestOrderConfirmationResponse;
import com.david.rebbystorebackend.dto.order.OrderItemResponse;
import com.david.rebbystorebackend.dto.order.OrderResponse;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class GuestCheckoutService {

    private static final List<String> TANZANIA_REGIONS = List.of(
            "Dar es Salaam", "Arusha", "Mwanza", "Dodoma", "Mbeya",
            "Morogoro", "Tanga", "Zanzibar", "Kilimanjaro", "Pwani",
            "Iringa", "Kagera", "Shinyanga", "Tabora", "Rukwa",
            "Ruvuma", "Lindi", "Mtwara", "Singida", "Kigoma",
            "Geita", "Simiyu", "Manyara", "Katavi", "Njombe"
    );

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final GuestCheckoutTokenService tokenService;

    public GuestCheckoutService(
            CustomerRepository customerRepository,
            CustomerService customerService,
            ProductRepository productRepository,
            OrderService orderService,
            GuestCheckoutTokenService tokenService
    ) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.productRepository = productRepository;
        this.orderService = orderService;
        this.tokenService = tokenService;
    }

    /** Creates the guest order and all order items in one transaction. */
    @Transactional
    public GuestCheckoutResponse checkout(GuestCheckoutRequest request) {
        String region = normalizeRegion(request.delivery().region());
        Map<Long, Integer> requestedQuantities = aggregateItems(request.items());

        // Lock products in ID order and validate the whole cart before creating records.
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "One of the selected products no longer exists"
                    ));

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A selected product is currently unavailable"
                );
            }
            if (product.getStockQuantity() == null
                    || product.getStockQuantity() < entry.getValue()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "There is not enough stock for " + product.getName()
                );
            }
        }

        GuestCheckoutRequest.Customer customerInput = request.customer();
        Customer customer = resolveCustomer(customerInput);
        GuestCheckoutRequest.Delivery delivery = request.delivery();

        Order order = orderService.createOrder(
                customer.getId(),
                delivery.address().trim(),
                delivery.city().trim(),
                region,
                normalizeOptional(delivery.notes()),
                PaymentMethod.CASH_ON_DELIVERY
        );

        // OrderService derives prices from Product and recalculates totals server-side.
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            orderService.addItem(order.getId(), entry.getKey(), entry.getValue());
        }

        Order completedOrder = orderService.getById(order.getId());
        List<OrderItem> savedItems = orderService.getItems(order.getId());
        GuestOrderConfirmationResponse confirmation = toConfirmation(
                completedOrder,
                savedItems
        );

        return new GuestCheckoutResponse(
                tokenService.createToken(completedOrder.getId()),
                confirmation
        );
    }

    @Transactional(readOnly = true)
    public GuestOrderConfirmationResponse getConfirmation(String token) {
        Long orderId = tokenService.requireOrderId(token);
        Order order = orderService.getById(orderId);
        return toConfirmation(order, orderService.getItems(orderId));
    }

    private Customer resolveCustomer(GuestCheckoutRequest.Customer input) {
        String phone = normalizePhone(input.phone());
        String email = input.email().trim().toLowerCase(Locale.ROOT);

        Optional<Customer> byPhone = customerRepository.findByPhone(phone);
        Optional<Customer> byEmail = customerRepository.findByEmailIgnoreCase(email);

        if (byPhone.isPresent() || byEmail.isPresent()) {
            if (byPhone.isPresent() && byEmail.isPresent()
                    && byPhone.get().getId().equals(byEmail.get().getId())) {
                return byPhone.get();
            }
            // Do not expose customer lookup data or attach an order to a mismatched record.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "These contact details cannot be matched to one customer. Check the phone and email."
            );
        }

        return customerService.createCustomer(
                input.fullName().trim(),
                phone,
                email
        );
    }

    private Map<Long, Integer> aggregateItems(List<GuestCheckoutRequest.Item> items) {
        Map<Long, Integer> quantities = new TreeMap<>();
        for (GuestCheckoutRequest.Item item : items) {
            if (item == null || item.productId() == null || item.quantity() == null
                    || item.productId() <= 0 || item.quantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each item must have a positive product ID and quantity"
                );
            }
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private String normalizePhone(String value) {
        String phone = value.trim().replaceAll("[\\s()-]", "");
        if (phone.startsWith("+255")) {
            phone = "0" + phone.substring(4);
        } else if (phone.startsWith("255")) {
            phone = "0" + phone.substring(3);
        }
        if (!phone.matches("^(06|07)\\d{8}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter a valid Tanzanian mobile number"
            );
        }
        return phone;
    }

    private String normalizeRegion(String requestedRegion) {
        String value = requestedRegion.trim();
        return TANZANIA_REGIONS.stream()
                .filter(region -> region.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Select a valid delivery region"
                ));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private GuestOrderConfirmationResponse toConfirmation(
            Order order,
            List<OrderItem> items
    ) {
        return new GuestOrderConfirmationResponse(
                OrderResponse.from(order),
                items.stream().map(OrderItemResponse::from).toList()
        );
    }
}
