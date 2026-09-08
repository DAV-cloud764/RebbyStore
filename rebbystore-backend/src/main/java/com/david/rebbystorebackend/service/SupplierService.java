package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Supplier;
import com.david.rebbystorebackend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier create(
            String name,
            String phone,
            String email,
            String address
    ) {
        String normalizedName = normalizeRequired(name, "Supplier name");
        String normalizedPhone = normalizePhone(phone);
        String normalizedEmail = normalizeEmail(email);
        String normalizedAddress = normalizeOptional(address);

        if (supplierRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Supplier with name '" + normalizedName + "' already exists"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Supplier supplier = new Supplier();
        supplier.setName(normalizedName);
        supplier.setPhone(normalizedPhone);
        supplier.setEmail(normalizedEmail);
        supplier.setAddress(normalizedAddress);
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Supplier with id " + id + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Supplier getByName(String name) {
        String normalizedName = normalizeRequired(name, "Supplier name");

        return supplierRepository.findByNameIgnoreCase(normalizedName)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Supplier with name '" +
                                        normalizedName +
                                        "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return supplierRepository.findAllByOrderByNameAsc();
    }

    public Supplier update(
            Long id,
            String name,
            String phone,
            String email,
            String address
    ) {
        Supplier supplier = getById(id);

        String normalizedName = normalizeRequired(name, "Supplier name");
        String normalizedPhone = normalizePhone(phone);
        String normalizedEmail = normalizeEmail(email);
        String normalizedAddress = normalizeOptional(address);

        boolean nameChanged =
                !normalizedName.equalsIgnoreCase(supplier.getName());

        if (nameChanged &&
                supplierRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Supplier with name '" + normalizedName + "' already exists"
            );
        }

        supplier.setName(normalizedName);
        supplier.setPhone(normalizedPhone);
        supplier.setEmail(normalizedEmail);
        supplier.setAddress(normalizedAddress);
        supplier.setUpdatedAt(OffsetDateTime.now());

        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        Supplier supplier = getById(id);

        supplierRepository.delete(supplier);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String normalized = phone.replaceAll("\\s+", "");

        if (normalized.startsWith("+255")) {
            normalized = "0" + normalized.substring(4);
        } else if (normalized.startsWith("255")) {
            normalized = "0" + normalized.substring(3);
        }

        if (!normalized.matches("07\\d{8}")) {
            throw new IllegalArgumentException(
                    "Invalid Tanzanian mobile phone number"
            );
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeOptional(email);

        if (normalized == null) {
            return null;
        }

        normalized = normalized.toLowerCase();

        if (!normalized.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            throw new IllegalArgumentException(
                    "Invalid email address"
            );
        }

        return normalized;
    }
}