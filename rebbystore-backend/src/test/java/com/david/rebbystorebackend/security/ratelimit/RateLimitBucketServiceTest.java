package com.david.rebbystorebackend.security.ratelimit;

import com.david.rebbystorebackend.config.RateLimitProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitBucketServiceTest {

    private RateLimitBucketService createService(
            int maxClients
    ) {
        RateLimitProperties properties =
                new RateLimitProperties(
                        true,
                        new RateLimitProperties.Rule(10, 1),
                        new RateLimitProperties.Rule(120, 1),
                        maxClients,
                        10
                );

        return new RateLimitBucketService(properties);
    }

    @Test
    void shouldAllowLoginRequestsUntilCapacityIsExhausted() {
        RateLimitBucketService service =
                createService(100);

        for (int i = 0; i < 10; i++) {
            RateLimitDecision decision =
                    service.tryConsume(
                            "192.168.1.10",
                            RateLimitType.LOGIN
                    );

            assertThat(decision.allowed()).isTrue();
            assertThat(decision.limit()).isEqualTo(10);
        }

        RateLimitDecision blocked =
                service.tryConsume(
                        "192.168.1.10",
                        RateLimitType.LOGIN
                );

        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.limit()).isEqualTo(10);
        assertThat(blocked.remainingTokens()).isZero();
        assertThat(blocked.retryAfterSeconds())
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldKeepGeneralAndLoginBucketsIndependent() {
        RateLimitBucketService service =
                createService(100);

        for (int i = 0; i < 10; i++) {
            RateLimitDecision decision =
                    service.tryConsume(
                            "192.168.1.11",
                            RateLimitType.LOGIN
                    );

            assertThat(decision.allowed()).isTrue();
        }

        RateLimitDecision loginBlocked =
                service.tryConsume(
                        "192.168.1.11",
                        RateLimitType.LOGIN
                );

        RateLimitDecision generalAllowed =
                service.tryConsume(
                        "192.168.1.11",
                        RateLimitType.GENERAL
                );

        assertThat(loginBlocked.allowed()).isFalse();
        assertThat(generalAllowed.allowed()).isTrue();
        assertThat(generalAllowed.limit())
                .isEqualTo(120);
    }

    @Test
    void shouldTrackDifferentClientsIndependently() {
        RateLimitBucketService service =
                createService(100);

        RateLimitDecision firstClient =
                service.tryConsume(
                        "192.168.1.20",
                        RateLimitType.LOGIN
                );

        RateLimitDecision secondClient =
                service.tryConsume(
                        "192.168.1.21",
                        RateLimitType.LOGIN
                );

        assertThat(firstClient.allowed()).isTrue();
        assertThat(secondClient.allowed()).isTrue();

        assertThat(firstClient.remainingTokens())
                .isEqualTo(9);

        assertThat(secondClient.remainingTokens())
                .isEqualTo(9);
    }

    @Test
    void shouldRespectMaximumClientRegistrySize() {
        RateLimitBucketService service =
                createService(2);

        service.tryConsume(
                "192.168.1.30",
                RateLimitType.GENERAL
        );

        service.tryConsume(
                "192.168.1.31",
                RateLimitType.GENERAL
        );

        service.tryConsume(
                "192.168.1.32",
                RateLimitType.GENERAL
        );

        assertThat(service.clientCountForTesting())
                .isLessThanOrEqualTo(2);
    }

    @Test
    void shouldAllowAllRequestsWhenRateLimitingIsDisabled() {
        RateLimitProperties properties =
                new RateLimitProperties(
                        false,
                        new RateLimitProperties.Rule(10, 1),
                        new RateLimitProperties.Rule(120, 1),
                        100,
                        10
                );

        RateLimitBucketService service =
                new RateLimitBucketService(properties);

        for (int i = 0; i < 20; i++) {
            RateLimitDecision decision =
                    service.tryConsume(
                            "192.168.1.40",
                            RateLimitType.LOGIN
                    );

            assertThat(decision.allowed()).isTrue();
        }
    }
}