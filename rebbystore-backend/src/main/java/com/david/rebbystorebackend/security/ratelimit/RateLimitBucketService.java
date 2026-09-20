package com.david.rebbystorebackend.security.ratelimit;

import com.david.rebbystorebackend.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitBucketService {

    private static final String LOGIN_PREFIX = "login:";
    private static final String GENERAL_PREFIX = "general:";

    private final RateLimitProperties properties;

    private final Map<String, ClientBuckets> clients =
            new ConcurrentHashMap<>();

    public RateLimitBucketService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public RateLimitDecision tryConsume(
            String clientKey,
            RateLimitType type
    ) {
        if (!properties.enabled()) {
            return new RateLimitDecision(
                    true,
                    getRule(type).capacity(),
                    Long.MAX_VALUE,
                    0
            );
        }

        ClientBuckets clientBuckets = getClientBuckets(clientKey);

        Bucket bucket = switch (type) {
            case LOGIN -> clientBuckets.loginBucket();
            case GENERAL -> clientBuckets.generalBucket();
        };

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long remaining =
                Math.max(0, probe.getRemainingTokens());

        long limit = getRule(type).capacity();

        if (probe.isConsumed()) {
            return new RateLimitDecision(
                    true,
                    limit,
                    remaining,
                    0
            );
        }

        return new RateLimitDecision(
                false,
                limit,
                remaining,
                toRetryAfterSeconds(
                        probe.getNanosToWaitForRefill()
                )
        );
    }

    private ClientBuckets getClientBuckets(String clientKey) {
        ClientBuckets existing = clients.get(clientKey);

        if (existing != null) {
            existing.touch();
            return existing;
        }

        synchronized (clients) {
            existing = clients.get(clientKey);

            if (existing != null) {
                existing.touch();
                return existing;
            }

            cleanupExpiredClients();

            if (clients.size() >= properties.maxClients()) {
                evictLeastRecentlyUsedClient();
            }

            ClientBuckets created = new ClientBuckets(
                    createBucket(properties.login()),
                    createBucket(properties.general())
            );

            clients.put(clientKey, created);

            return created;
        }
    }

    private Bucket createBucket(
            RateLimitProperties.Rule rule
    ) {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(rule.capacity())
                        .refillGreedy(
                                rule.capacity(),
                                Duration.ofMinutes(
                                        rule.refillMinutes()
                                )
                        )
                )
                .build();
    }

    private RateLimitProperties.Rule getRule(
            RateLimitType type
    ) {
        return switch (type) {
            case LOGIN -> properties.login();
            case GENERAL -> properties.general();
        };
    }

    @Scheduled(
            fixedDelayString =
                    "${rate-limit.cleanup-minutes:10}m"
    )
    public void cleanupExpiredClients() {
        long expirationMillis =
                Duration.ofMinutes(
                        properties.cleanupMinutes()
                ).toMillis();

        long cutoff =
                System.currentTimeMillis() - expirationMillis;

        clients.entrySet().removeIf(entry ->
                entry.getValue().lastAccessTime() < cutoff
        );
    }

    private void evictLeastRecentlyUsedClient() {
        String leastRecentlyUsedKey = null;
        long oldestAccess = Long.MAX_VALUE;

        for (Map.Entry<String, ClientBuckets> entry :
                clients.entrySet()) {

            long lastAccess =
                    entry.getValue().lastAccessTime();

            if (lastAccess < oldestAccess) {
                oldestAccess = lastAccess;
                leastRecentlyUsedKey = entry.getKey();
            }
        }

        if (leastRecentlyUsedKey != null) {
            clients.remove(leastRecentlyUsedKey);
        }
    }

    private long toRetryAfterSeconds(long nanos) {
        long seconds =
                TimeUnit.NANOSECONDS.toSeconds(nanos);

        if (nanos % TimeUnit.SECONDS.toNanos(1) != 0) {
            seconds++;
        }

        return Math.max(1, seconds);
    }

    int clientCountForTesting() {
        return clients.size();
    }

    private static final class ClientBuckets {

        private final Bucket loginBucket;
        private final Bucket generalBucket;

        private volatile long lastAccessTime;

        private ClientBuckets(
                Bucket loginBucket,
                Bucket generalBucket
        ) {
            this.loginBucket = loginBucket;
            this.generalBucket = generalBucket;
            this.lastAccessTime =
                    System.currentTimeMillis();
        }

        private Bucket loginBucket() {
            touch();
            return loginBucket;
        }

        private Bucket generalBucket() {
            touch();
            return generalBucket;
        }

        private void touch() {
            lastAccessTime =
                    System.currentTimeMillis();
        }

        private long lastAccessTime() {
            return lastAccessTime;
        }
    }
}