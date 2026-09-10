package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
import com.david.rebbystorebackend.exception.ConflictException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SupplierServiceTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {
        supplierRepository.deleteAll();
    }

    @Test
    void shouldCreateSupplier() {
        Supplier supplier = supplierService.create(
                "Luxury Hair Supplier",
                "+255712345678",
                "SUPPLIER@EMAIL.COM",
                "Dar es Salaam"
        );

        assertThat(supplier.getId()).isNotNull();
        assertThat(supplier.getName())
                .isEqualTo("Luxury Hair Supplier");
        assertThat(supplier.getPhone())
                .isEqualTo("0712345678");
        assertThat(supplier.getEmail())
                .isEqualTo("supplier@email.com");
        assertThat(supplier.getAddress())
                .isEqualTo("Dar es Salaam");
        assertThat(supplier.getCreatedAt()).isNotNull();
        assertThat(supplier.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldGetSupplierById() {
        Supplier created = supplierService.create(
                "Luxury Hair Supplier",
                "0712345678",
                "supplier@example.com",
                "Dar es Salaam"
        );

        Supplier found = supplierService.getById(created.getId());

        assertThat(found.getName())
                .isEqualTo("Luxury Hair Supplier");
    }

    @Test
    void shouldGetSupplierByNameIgnoringCase() {
        supplierService.create(
                "Luxury Hair Supplier",
                "0712345678",
                "supplier@example.com",
                "Dar es Salaam"
        );

        Supplier found =
                supplierService.getByName("luxury hair supplier");

        assertThat(found.getName())
                .isEqualTo("Luxury Hair Supplier");
    }

    @Test
    void shouldReturnAllSuppliersOrderedByName() {
        supplierService.create(
                "Zebra Hair",
                "0712345678",
                null,
                null
        );

        supplierService.create(
                "Alpha Hair",
                "0712345679",
                null,
                null
        );

        supplierService.create(
                "Beauty Hair",
                "0712345680",
                null,
                null
        );

        List<Supplier> suppliers = supplierService.getAll();

        assertThat(suppliers)
                .extracting(Supplier::getName)
                .containsExactly(
                        "Alpha Hair",
                        "Beauty Hair",
                        "Zebra Hair"
                );
    }

    @Test
    void shouldUpdateSupplier() {
        Supplier created = supplierService.create(
                "Old Supplier",
                "0712345678",
                "old@example.com",
                "Old Address"
        );

        Supplier updated = supplierService.update(
                created.getId(),
                "New Supplier",
                "255712345679",
                "NEW@EXAMPLE.COM",
                "New Address"
        );

        assertThat(updated.getName())
                .isEqualTo("New Supplier");
        assertThat(updated.getPhone())
                .isEqualTo("0712345679");
        assertThat(updated.getEmail())
                .isEqualTo("new@example.com");
        assertThat(updated.getAddress())
                .isEqualTo("New Address");
    }

    @Test
    void shouldRejectDuplicateSupplierName() {
        supplierService.create(
                "Luxury Hair",
                "0712345678",
                null,
                null
        );

        assertThatThrownBy(() ->
                supplierService.create(
                        "luxury hair",
                        "0712345679",
                        null,
                        null
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(
                        "already exists"
                );
    }

    @Test
    void shouldRejectBlankSupplierName() {
        assertThatThrownBy(() ->
                supplierService.create(
                        "   ",
                        "0712345678",
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Supplier name must not be blank"
                );
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThatThrownBy(() ->
                supplierService.create(
                        "Luxury Hair",
                        "12345",
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Invalid Tanzanian mobile phone number"
                );
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() ->
                supplierService.create(
                        "Luxury Hair",
                        "0712345678",
                        "invalid-email",
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Invalid email address"
                );
    }

    @Test
    void shouldRejectNonExistingSupplier() {
        assertThatThrownBy(() -> supplierService.getById(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Supplier with id 999999 not found");
    }

    @Test
    void shouldDeleteSupplier() {
        Supplier created = supplierService.create(
                "Luxury Hair",
                "0712345678",
                null,
                null
        );

        supplierService.delete(created.getId());

        assertThat(supplierRepository.findById(created.getId()))
                .isEmpty();
    }

    @Test
    void shouldAllowSupplierWithoutOptionalFields() {
        Supplier supplier = supplierService.create(
                "Luxury Hair",
                null,
                null,
                null
        );

        assertThat(supplier.getPhone()).isNull();
        assertThat(supplier.getEmail()).isNull();
        assertThat(supplier.getAddress()).isNull();
    }
}