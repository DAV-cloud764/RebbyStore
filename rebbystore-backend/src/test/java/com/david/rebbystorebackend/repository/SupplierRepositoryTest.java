package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindSupplierByNameIgnoringCase() {

        Long supplierId = createSupplier(
                "Beauty Hair Supplier",
                "0712340001",
                "supplier@example.com"
        );

        var result = supplierRepository.findByNameIgnoreCase(
                "beauty hair supplier"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(supplierId);
        assertThat(result.get().getName())
                .isEqualTo("Beauty Hair Supplier");
    }

    @Test
    void shouldCheckWhetherSupplierNameExistsIgnoringCase() {

        createSupplier(
                "Existing Supplier",
                "0712340002",
                "existing@example.com"
        );

        assertThat(
                supplierRepository.existsByNameIgnoreCase(
                        "EXISTING SUPPLIER"
                )
        ).isTrue();

        assertThat(
                supplierRepository.existsByNameIgnoreCase(
                        "Missing Supplier"
                )
        ).isFalse();
    }

    @Test
    void shouldFindAllSuppliersOrderedByName() {

        createSupplier(
                "Zeta Supplier",
                "0712340003",
                "zeta@example.com"
        );

        createSupplier(
                "Alpha Supplier",
                "0712340004",
                "alpha@example.com"
        );

        createSupplier(
                "Beta Supplier",
                "0712340005",
                "beta@example.com"
        );

        List<Supplier> results =
                supplierRepository.findAllByOrderByNameAsc();

        assertThat(results)
                .extracting(Supplier::getName)
                .containsSequence(
                        "Alpha Supplier",
                        "Beta Supplier",
                        "Zeta Supplier"
                );
    }

    private Long createSupplier(
            String name,
            String phone,
            String email
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO suppliers (
                    name,
                    phone,
                    email
                )
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                name,
                phone,
                email
        );
    }
}