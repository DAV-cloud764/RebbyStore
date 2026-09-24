package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Customer;
import com.david.rebbystorebackend.dto.customer.CustomerCreateRequest;
import com.david.rebbystorebackend.dto.customer.CustomerResponse;
import com.david.rebbystorebackend.dto.customer.CustomerUpdateRequest;
import com.david.rebbystorebackend.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long id
    ) {
        Customer customer = customerService.getCustomerById(id);

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(
            @PathVariable String phone
    ) {
        Customer customer = customerService.getCustomerByPhone(phone);

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> response = customerService
                .getAllCustomers()
                .stream()
                .map(CustomerResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(
            @PathVariable String email
    ) {
        Customer customer = customerService.getCustomerByEmail(email);

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request
    ) {
        Customer customer = customerService.createCustomer(
                request.fullName(),
                request.phone(),
                request.email()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerResponse.from(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        Customer customer = customerService.updateCustomer(
                id,
                request.fullName(),
                request.phone(),
                request.email()
        );

        return ResponseEntity.ok(
                CustomerResponse.from(customer)
        );
    }
}