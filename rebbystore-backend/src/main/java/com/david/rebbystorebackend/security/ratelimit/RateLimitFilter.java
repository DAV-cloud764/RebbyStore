package com.david.rebbystorebackend.security.ratelimit;

import com.david.rebbystorebackend.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String HEADER_LIMIT = "RateLimit-Limit";
    private static final String HEADER_REMAINING = "RateLimit-Remaining";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private final RateLimitBucketService bucketService;
    private final RateLimitProperties properties;

    public RateLimitFilter(
            RateLimitBucketService bucketService,
            RateLimitProperties properties
    ) {
        this.bucketService = bucketService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.enabled()
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitType type = resolveRateLimitType(request);

        if (type == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);

        RateLimitDecision decision =
                bucketService.tryConsume(clientKey, type);

        response.setHeader(
                HEADER_LIMIT,
                String.valueOf(decision.limit())
        );

        response.setHeader(
                HEADER_REMAINING,
                String.valueOf(decision.remainingTokens())
        );

        if (!decision.allowed()) {
            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setHeader(
                    HEADER_RETRY_AFTER,
                    String.valueOf(
                            decision.retryAfterSeconds()
                    )
            );

            response.setContentType("application/json");

            response.getWriter().write(
                    """
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Rate limit exceeded",
                      "path": "%s"
                    }
                    """.formatted(request.getRequestURI())
            );

            log.warn(
                    "Rate limit exceeded: client={}, type={}, method={}, path={}",
                    clientKey,
                    type,
                    request.getMethod(),
                    request.getRequestURI()
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitType resolveRateLimitType(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        if ("/api/auth/login".equals(path)
                && "POST".equalsIgnoreCase(request.getMethod())) {
            return RateLimitType.LOGIN;
        }

        if (path.startsWith("/api/")) {
            return RateLimitType.GENERAL;
        }

        return null;
    }

    private String resolveClientKey(
            HttpServletRequest request
    ) {
        return request.getRemoteAddr();
    }
}