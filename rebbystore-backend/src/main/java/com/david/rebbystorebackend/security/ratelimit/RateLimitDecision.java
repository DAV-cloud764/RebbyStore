package com.david.rebbystorebackend.security.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        long limit,
        long remainingTokens,
        long retryAfterSeconds
) {
}