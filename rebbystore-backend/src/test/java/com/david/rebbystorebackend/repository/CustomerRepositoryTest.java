package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindCustomerByPhone() {

        Long customerId = createCustomer(
                "David Customer",
                "0712345678",
                "david@example.com"
        );

        var result = customerRepository.findByPhone("0712345678");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(customerId);
        assertThat(result.get().getFullName()).isEqualTo("David Customer");
    }

    @Test
    void shouldFindCustomerByEmailIgnoringCase() {

        Long customerId = createCustomer(
                "Email Customer",
                "0755555555",
                "David.Customer@Example.com"
        );

        var result = customerRepository.findByEmailIgnoreCase(
                "david.customer@example.com"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(customerId);
    }

    @Test
    void shouldCheckWhetherPhoneExists() {

        createCustomer(
                "Phone Customer",
                "0766666666",
                "phone@example.com"
        );

        assertThat(customerRepository.existsByPhone("0766666666"))
                .isTrue();

        assertThat(customerRepository.existsByPhone("0788888888"))
                .isFalse();
    }

    @Test
    void shouldCheckWhetherEmailExistsIgnoringCase() {

        createCustomer(
                "Email Exists Customer",
                "0799999999",
                "Customer@Example.com"
        );

        assertThat(
                customerRepository.existsByEmailIgnoreCase(
                        "customer@example.com"
                )
        ).isTrue();

        assertThat(
                customerRepository.existsByEmailIgnoreCase(
                        "missing@example.com"
                )
        ).isFalse();
    }

    private Long createCustomer(
            String fullName,
            String phone,
            String email
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO customers (
                    full_name,
                    phone,
                    email
                )
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                fullName,
                phone,
                email
        );
    }
}