package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.exception.ConflictException;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
import com.david.rebbystorebackend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomer() {
        Customer customer = customerService.createCustomer(
                "David Philanthropist",
                "0712345678",
                "david@example.com"
        );

        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getFullName()).isEqualTo("David Philanthropist");
        assertThat(customer.getPhone()).isEqualTo("0712345678");
        assertThat(customer.getEmail()).isEqualTo("david@example.com");
        assertThat(customer.getTotalOrders()).isEqualTo(0);
        assertThat(customer.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNormalizePhoneNumber() {
        Customer customer = customerService.createCustomer(
                "David",
                "+255 712 345 678",
                "david@example.com"
        );

        assertThat(customer.getPhone()).isEqualTo("0712345678");
    }

    @Test
    void shouldNormalizeEmail() {
        Customer customer = customerService.createCustomer(
                "David",
                "0712345678",
                "  DAVID@EXAMPLE.COM  "
        );

        assertThat(customer.getEmail()).isEqualTo("david@example.com");
    }

    @Test
    void shouldRejectDuplicatePhone() {
        customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "Another Customer",
                        "0712345678",
                        "another@example.com"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "Another Customer",
                        "0712345679",
                        "DAVID@EXAMPLE.COM"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCustomerById() {
        Customer created = customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        Customer found = customerService.getCustomerById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getFullName()).isEqualTo("David");
        assertThat(found.getPhone()).isEqualTo("0712345678");
        assertThat(found.getEmail()).isEqualTo("david@example.com");
    }

    @Test
    void shouldRejectMissingCustomerId() {
        assertThatThrownBy(() ->
                customerService.getCustomerById(999999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldGetCustomerByPhone() {
        customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        Customer found = customerService.getCustomerByPhone(
                "+255 712 345 678"
        );

        assertThat(found.getFullName()).isEqualTo("David");
        assertThat(found.getPhone()).isEqualTo("0712345678");
    }

    @Test
    void shouldRejectMissingCustomerPhone() {
        assertThatThrownBy(() ->
                customerService.getCustomerByPhone("0712345679")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldGetCustomerByEmail() {
        customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        Customer found = customerService.getCustomerByEmail(
                "DAVID@EXAMPLE.COM"
        );

        assertThat(found.getFullName()).isEqualTo("David");
        assertThat(found.getEmail()).isEqualTo("david@example.com");
    }

    @Test
    void shouldRejectMissingCustomerEmail() {
        assertThatThrownBy(() ->
                customerService.getCustomerByEmail("missing@example.com")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldUpdateCustomer() {
        Customer created = customerService.createCustomer(
                "David",
                "0712345678",
                "david@example.com"
        );

        Customer updated = customerService.updateCustomer(
                created.getId(),
                "David Updated",
                "+255 713 456 789",
                "updated@example.com"
        );

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getFullName()).isEqualTo("David Updated");
        assertThat(updated.getPhone()).isEqualTo("0713456789");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectUpdatingToExistingPhone() {
        Customer first = customerService.createCustomer(
                "First Customer",
                "0712345678",
                "one@example.com"
        );

        customerService.createCustomer(
                "Second Customer",
                "0712345679",
                "two@example.com"
        );

        assertThatThrownBy(() ->
                customerService.updateCustomer(
                        first.getId(),
                        "First Customer",
                        "0712345679",
                        "one@example.com"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectUpdatingToExistingEmail() {
        Customer first = customerService.createCustomer(
                "First Customer",
                "0712345678",
                "one@example.com"
        );

        customerService.createCustomer(
                "Second Customer",
                "0712345679",
                "two@example.com"
        );

        assertThatThrownBy(() ->
                customerService.updateCustomer(
                        first.getId(),
                        "First Customer",
                        "0712345678",
                        "two@example.com"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectBlankCustomerName() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "",
                        "0712345678",
                        "david@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("full name is required");
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "David",
                        "1234567890",
                        "david@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid Tanzanian mobile number");
    }

    private Customer createCustomer(
            String fullName,
            String phone,
            String email
    ) {
        Customer customer = new Customer();

        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);

        OffsetDateTime now = OffsetDateTime.now();

        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        return customerRepository.save(customer);
    }
}