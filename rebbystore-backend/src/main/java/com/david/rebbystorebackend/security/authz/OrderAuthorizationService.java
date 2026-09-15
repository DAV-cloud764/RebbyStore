package com.david.rebbystorebackend.security.authz;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.david.rebbystorebackend.repository.OrderItemRepository;

@Service
public class OrderAuthorizationService {

    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderAuthorizationService(
            CustomerRepository customerRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void assertCanAccessOrderItem(
            UserPrincipal principal,
            Long orderItemId
    ) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Order ownership could not be verified"
                        )
                );

        assertCanAccessOrder(principal, item.getOrder());
    }

    public void assertCanAccessCustomer(
            UserPrincipal principal,
            Long customerId
    ) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        if (hasStaffAccess(principal)) {
            return;
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not allowed to access this customer's orders"
                        )
                );

        String principalEmail = principal.getEmail();

        if (principalEmail == null
                || customer.getEmail() == null
                || !principalEmail.equalsIgnoreCase(customer.getEmail())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this customer's orders"
            );
        }
    }

    public void assertCanAccessOrder(
            UserPrincipal principal,
            Order order
    ) {
        if (hasStaffAccess(principal)) {
            return;
        }

        if (order == null || order.getCustomer() == null) {
            throw new AccessDeniedException(
                    "Order ownership could not be verified"
            );
        }

        assertCanAccessCustomer(
                principal,
                order.getCustomer().getId()
        );
    }

    public void assertCanAccessOrderItem(
            UserPrincipal principal,
            OrderItem item
    ) {
        if (item == null || item.getOrder() == null) {
            throw new AccessDeniedException(
                    "Order ownership could not be verified"
            );
        }

        assertCanAccessOrder(
                principal,
                item.getOrder()
        );
    }

    public boolean hasStaffAccess(UserPrincipal principal) {
        if (principal == null) {
            return false;
        }

        return principal.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                                || authority.getAuthority().equals("ROLE_STAFF")
                );
    }
}