package com.david.rebbystorebackend.security.authz;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.domain.entity.Order;
import com.david.rebbystorebackend.domain.entity.OrderItem;
import com.david.rebbystorebackend.repository.CustomerRepository;
import com.david.rebbystorebackend.repository.OrderItemRepository;
import com.david.rebbystorebackend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAuthorizationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private OrderAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService =
                new OrderAuthorizationService(
                        customerRepository,
                        orderItemRepository
                );
    }

    // ---------------------------------------------------------
    // Customer ownership authorization
    // ---------------------------------------------------------

    @Test
    void customerShouldAccessOwnOrders() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        Customer customer = mock(Customer.class);

        when(customer.getEmail())
                .thenReturn("customer@example.com");

        when(customerRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatCode(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        10L
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void customerShouldNotAccessAnotherCustomersOrders() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        Customer otherCustomer = mock(Customer.class);

        when(otherCustomer.getEmail())
                .thenReturn("other@example.com");

        when(customerRepository.findById(20L))
                .thenReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        20L
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "You are not allowed to access this customer's orders"
                );
    }

    @Test
    void staffShouldAccessAnyCustomersOrders() {
        UserPrincipal principal = createPrincipal(
                30L,
                "staff",
                "staff@example.com",
                "ROLE_STAFF"
        );

        assertThatCode(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        999L
                )
        ).doesNotThrowAnyException();

        verifyNoInteractions(customerRepository);
    }

    @Test
    void adminShouldAccessAnyCustomersOrders() {
        UserPrincipal principal = createPrincipal(
                40L,
                "admin",
                "admin@example.com",
                "ROLE_ADMIN"
        );

        assertThatCode(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        999L
                )
        ).doesNotThrowAnyException();

        verifyNoInteractions(customerRepository);
    }

    @Test
    void shouldRejectMissingPrincipal() {
        assertThatThrownBy(() ->
                authorizationService.assertCanAccessCustomer(
                        null,
                        10L
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authentication is required");

        verifyNoInteractions(customerRepository);
    }

    @Test
    void shouldRejectMissingCustomer() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        when(customerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        99L
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "You are not allowed to access this customer's orders"
                );
    }

    @Test
    void shouldCompareCustomerEmailsIgnoringCase() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "Customer@Example.com",
                "ROLE_CUSTOMER"
        );

        Customer customer = mock(Customer.class);

        when(customer.getEmail())
                .thenReturn("customer@example.com");

        when(customerRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatCode(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        10L
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCustomerWhenPrincipalEmailIsMissing() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                null,
                "ROLE_CUSTOMER"
        );

        Customer customer = mock(Customer.class);

        when(customerRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        10L
                )
        )
                .isInstanceOf(AccessDeniedException.class);

        verify(customerRepository).findById(10L);
    }

    @Test
    void shouldRejectCustomerWhenCustomerEmailIsMissing() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        Customer customer = mock(Customer.class);

        when(customer.getEmail())
                .thenReturn(null);

        when(customerRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessCustomer(
                        principal,
                        10L
                )
        )
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------------------------------------------------------
    // Order-item ownership authorization
    // ---------------------------------------------------------

    @Test
    void customerShouldAccessOwnOrderItem() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        Customer customer = mock(Customer.class);

        when(customer.getId())
                .thenReturn(10L);

        when(customer.getEmail())
                .thenReturn("customer@example.com");

        Order order = mock(Order.class);

        when(order.getCustomer())
                .thenReturn(customer);

        OrderItem orderItem = mock(OrderItem.class);

        when(orderItem.getOrder())
                .thenReturn(order);

        when(orderItemRepository.findById(100L))
                .thenReturn(Optional.of(orderItem));

        when(customerRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatCode(() ->
                authorizationService.assertCanAccessOrderItem(
                        principal,
                        100L
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void customerShouldNotAccessAnotherCustomersOrderItem() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        Customer otherCustomer = mock(Customer.class);

        when(otherCustomer.getId())
                .thenReturn(20L);

        when(otherCustomer.getEmail())
                .thenReturn("other@example.com");

        Order order = mock(Order.class);

        when(order.getCustomer())
                .thenReturn(otherCustomer);

        OrderItem orderItem = mock(OrderItem.class);

        when(orderItem.getOrder())
                .thenReturn(order);

        when(orderItemRepository.findById(100L))
                .thenReturn(Optional.of(orderItem));

        when(customerRepository.findById(20L))
                .thenReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessOrderItem(
                        principal,
                        100L
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "You are not allowed to access this customer's orders"
                );
    }

    @Test
    void staffShouldAccessAnyOrderItem() {
        UserPrincipal principal = createPrincipal(
                30L,
                "staff",
                "staff@example.com",
                "ROLE_STAFF"
        );

        OrderItem orderItem = mock(OrderItem.class);

        when(orderItemRepository.findById(100L))
                .thenReturn(Optional.of(orderItem));

        assertThatCode(() ->
                authorizationService.assertCanAccessOrderItem(
                        principal,
                        100L
                )
        ).doesNotThrowAnyException();

        verify(orderItemRepository).findById(100L);
    }

    @Test
    void shouldRejectMissingOrderItem() {
        UserPrincipal principal = createPrincipal(
                10L,
                "customer",
                "customer@example.com",
                "ROLE_CUSTOMER"
        );

        when(orderItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authorizationService.assertCanAccessOrderItem(
                        principal,
                        999L
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Order ownership could not be verified");
    }

    // ---------------------------------------------------------
    // Test helper
    // ---------------------------------------------------------

    private UserPrincipal createPrincipal(
            Long id,
            String username,
            String email,
            String role
    ) {
        return UserPrincipal.createForTesting(
                id,
                username,
                email,
                "$2a$10$dummy",
                true,
                List.of(
                        new SimpleGrantedAuthority(role)
                )
        );
    }
}