package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
                "David John",
                "0712 345 678",
                "David@Example.com"
        );

        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getFullName())
                .isEqualTo("David John");
        assertThat(customer.getPhone())
                .isEqualTo("0712345678");
        assertThat(customer.getEmail())
                .isEqualTo("david@example.com");
        assertThat(customer.getTotalOrders())
                .isZero();
        assertThat(customer.getTotalSpent())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNormalizeInternationalPhoneNumber() {
        Customer customer = customerService.createCustomer(
                "David John",
                "+255712345678",
                "david@example.com"
        );

        assertThat(customer.getPhone())
                .isEqualTo("0712345678");
    }

    @Test
    void shouldRejectDuplicatePhone() {
        customerService.createCustomer(
                "David John",
                "0712345678",
                "david@example.com"
        );

        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "Another Customer",
                        "0712 345 678",
                        "another@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        customerService.createCustomer(
                "David John",
                "0712345678",
                "david@example.com"
        );

        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "Another Customer",
                        "0755555555",
                        "DAVID@EXAMPLE.COM"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCustomerById() {
        Customer created = customerService.createCustomer(
                "David John",
                "0712345678",
                "david@example.com"
        );

        Customer found =
                customerService.getCustomerById(created.getId());

        assertThat(found.getFullName())
                .isEqualTo("David John");
    }

    @Test
    void shouldGetCustomerByPhone() {
        customerService.createCustomer(
                "David John",
                "0712345678",
                "david@example.com"
        );

        Customer found =
                customerService.getCustomerByPhone("0712 345 678");

        assertThat(found.getPhone())
                .isEqualTo("0712345678");
    }

    @Test
    void shouldGetCustomerByEmail() {
        customerService.createCustomer(
                "David John",
                "0712345678",
                "David@Example.com"
        );

        Customer found =
                customerService.getCustomerByEmail("DAVID@EXAMPLE.COM");

        assertThat(found.getEmail())
                .isEqualTo("david@example.com");
    }

    @Test
    void shouldUpdateCustomer() {
        Customer created = customerService.createCustomer(
                "David John",
                "0712345678",
                "david@example.com"
        );

        Customer updated = customerService.updateCustomer(
                created.getId(),
                "David Joseph",
                "0755555555",
                "david.joseph@example.com"
        );

        assertThat(updated.getFullName())
                .isEqualTo("David Joseph");
        assertThat(updated.getPhone())
                .isEqualTo("0755555555");
        assertThat(updated.getEmail())
                .isEqualTo("david.joseph@example.com");
    }

    @Test
    void shouldRejectUpdatingToExistingPhone() {
        customerService.createCustomer(
                "Customer One",
                "0712345678",
                "one@example.com"
        );

        Customer second = customerService.createCustomer(
                "Customer Two",
                "0755555555",
                "two@example.com"
        );

        assertThatThrownBy(() ->
                customerService.updateCustomer(
                        second.getId(),
                        "Customer Two Updated",
                        "0712 345 678",
                        "two.updated@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectUpdatingToExistingEmail() {
        customerService.createCustomer(
                "Customer One",
                "0712345678",
                "one@example.com"
        );

        Customer second = customerService.createCustomer(
                "Customer Two",
                "0755555555",
                "two@example.com"
        );

        assertThatThrownBy(() ->
                customerService.updateCustomer(
                        second.getId(),
                        "Customer Two Updated",
                        "0766666666",
                        "ONE@EXAMPLE.COM"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "David John",
                        "12345",
                        "david@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Phone must be a valid Tanzanian mobile number"
                );
    }

    @Test
    void shouldRejectBlankFullName() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        " ",
                        "0712345678",
                        "david@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer full name is required");
    }

    @Test
    void shouldRejectBlankPhone() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "David John",
                        " ",
                        "david@example.com"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer phone is required");
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() ->
                customerService.createCustomer(
                        "David John",
                        "0712345678",
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer email is required");
    }
}