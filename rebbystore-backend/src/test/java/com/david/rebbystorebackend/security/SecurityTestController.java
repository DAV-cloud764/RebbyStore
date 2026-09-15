package com.david.rebbystorebackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityTestController {

    @GetMapping("/test/protected")
    String protectedEndpoint(Authentication authentication) {
        return authentication == null
                ? "anonymous"
                : authentication.getName();
    }

    @GetMapping("/api/inventory/test")
    String inventoryEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/purchases/test")
    String purchasesEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/suppliers/test")
    String suppliersEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/customers/test")
    String customersEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/reporting/test")
    String reportingEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/orders/test")
    String ordersEndpoint(Authentication authentication) {
        return authentication.getName();
    }
}