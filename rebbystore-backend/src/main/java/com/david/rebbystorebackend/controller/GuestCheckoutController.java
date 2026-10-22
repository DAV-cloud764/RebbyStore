package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.dto.checkout.GuestCheckoutRequest;
import com.david.rebbystorebackend.dto.checkout.GuestCheckoutResponse;
import com.david.rebbystorebackend.dto.checkout.GuestOrderConfirmationResponse;
import com.david.rebbystorebackend.service.GuestCheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class GuestCheckoutController {

    private final GuestCheckoutService guestCheckoutService;

    public GuestCheckoutController(GuestCheckoutService guestCheckoutService) {
        this.guestCheckoutService = guestCheckoutService;
    }

    @PostMapping
    public ResponseEntity<GuestCheckoutResponse> checkout(
            @Valid @RequestBody GuestCheckoutRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guestCheckoutService.checkout(request));
    }

    @GetMapping("/confirmation/{token}")
    public ResponseEntity<GuestOrderConfirmationResponse> getConfirmation(
            @PathVariable String token
    ) {
        return ResponseEntity.ok(guestCheckoutService.getConfirmation(token));
    }
}
