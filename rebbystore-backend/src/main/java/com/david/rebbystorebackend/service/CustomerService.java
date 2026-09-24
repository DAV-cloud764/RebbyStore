package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.exception.ConflictException;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
import com.david.rebbystorebackend.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(
            String fullName,
            String phone,
            String email
    ) {
        validateRequiredFields(fullName, phone, email);

        String normalizedPhone = normalizePhone(phone);
        String normalizedEmail = normalizeEmail(email);

        if (customerRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException(
                    "Customer with phone '" + normalizedPhone + "' already exists"
            );
        }

        if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException(
                    "Customer with email '" + normalizedEmail + "' already exists"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Customer customer = new Customer();

        customer.setFullName(fullName.trim());
        customer.setPhone(normalizedPhone);
        customer.setEmail(normalizedEmail);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public java.util.List<Customer> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .sorted(java.util.Comparator.comparing(
                        Customer::getCreatedAt,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.reverseOrder()
                        )
                ))
                .toList();
    }
    public Customer getCustomerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer with ID '" + id + "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);

        return customerRepository.findByPhone(normalizedPhone)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer with phone '" +
                                        normalizedPhone +
                                        "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return customerRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer with email '" +
                                        normalizedEmail +
                                        "' not found"
                        )
                );
    }

    public Customer updateCustomer(
            Long id,
            String fullName,
            String phone,
            String email
    ) {
        validateRequiredFields(fullName, phone, email);

        Customer customer = getCustomerById(id);

        String normalizedPhone = normalizePhone(phone);
        String normalizedEmail = normalizeEmail(email);

        customerRepository.findByPhone(normalizedPhone)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Customer with phone '" +
                                    normalizedPhone +
                                    "' already exists"
                    );
                });

        customerRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Customer with email '" +
                                    normalizedEmail +
                                    "' already exists"
                    );
                });

        customer.setFullName(fullName.trim());
        customer.setPhone(normalizedPhone);
        customer.setEmail(normalizedEmail);
        customer.setUpdatedAt(OffsetDateTime.now());

        return customerRepository.save(customer);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Customer phone is required");
        }

        String normalized = phone.trim().replaceAll("[\\s()-]", "");

        if (normalized.startsWith("+255")) {
            normalized = "0" + normalized.substring(4);
        } else if (normalized.startsWith("255")) {
            normalized = "0" + normalized.substring(3);
        }

        if (!normalized.matches("^(06|07)\\d{8}$")) {
            throw new IllegalArgumentException(
                    "Phone must be a valid Tanzanian mobile number"
            );
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        return email.trim().toLowerCase();
    }

    private void validateRequiredFields(
            String fullName,
            String phone,
            String email
    ) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer full name is required"
            );
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer phone is required"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required"
            );
        }
    }
}