package com.david.rebbystorebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        boolean enabled,
        Rule login,
        Rule general,
        int maxClients,
        int cleanupMinutes
) {

    public record Rule(
            long capacity,
            long refillMinutes
    ) {
    }
}