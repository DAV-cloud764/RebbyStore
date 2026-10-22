package com.david.rebbystorebackend.dto.checkout;

/** Checkout result. The token grants access only to its associated receipt. */
public record GuestCheckoutResponse(
        String confirmationToken,
        GuestOrderConfirmationResponse confirmation
) {
}
