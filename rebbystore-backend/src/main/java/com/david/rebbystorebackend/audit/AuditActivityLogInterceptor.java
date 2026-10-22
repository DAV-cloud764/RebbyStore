package com.david.rebbystorebackend.audit;

import com.david.rebbystorebackend.service.AuditActivityLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.stream.Collectors;

@Component
public class AuditActivityLogInterceptor
        implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(AuditActivityLogInterceptor.class);

    private static final String START_TIME_ATTRIBUTE =
            AuditActivityLogInterceptor.class.getName() + ".startTime";

    private final AuditActivityLogService auditService;

    public AuditActivityLogInterceptor(
            AuditActivityLogService auditService
    ) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        request.setAttribute(
                START_TIME_ATTRIBUTE,
                System.nanoTime()
        );

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        if (!isWriteRequest(request)) {
            return;
        }

        try {
            long startTime = System.nanoTime();

            Object recordedStart =
                    request.getAttribute(START_TIME_ATTRIBUTE);

            if (recordedStart instanceof Long value) {
                startTime = value;
            }

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            int status = response.getStatus();

            if (exception != null && status < 400) {
                status = 500;
            }

            Authentication authentication =
                    SecurityContextHolder.getContext()
                            .getAuthentication();

            String username = null;
            String roles = null;

            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                username = authentication.getName();

                roles = authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
            }

            String path = getApplicationPath(request);

            auditService.recordActivity(
                    username,
                    roles,
                    determineModule(path),
                    determineAction(request.getMethod()),
                    request.getMethod(),
                    path,
                    status,
                    request.getRemoteAddr(),
                    durationMs
            );

        } catch (Exception auditException) {
            // Audit failure must not break the original API response.
            log.error(
                    "Failed to persist API activity audit record",
                    auditException
            );
        }
    }

    private boolean isWriteRequest(HttpServletRequest request) {
        String path = getApplicationPath(request);
        String method = request.getMethod();

        if (!path.startsWith("/api/")) {
            return false;
        }

        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/test/")) {
            return false;
        }

        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private String getApplicationPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null
                && !contextPath.isEmpty()
                && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        return path;
    }

    private String determineModule(String path) {
        if (path.startsWith("/api/products")
                || path.startsWith("/api/product-images")) {
            return "PRODUCTS";
        }

        if (path.startsWith("/api/categories")) {
            return "CATALOG";
        }

        if (path.startsWith("/api/inventory")) {
            return "INVENTORY";
        }

        if (path.startsWith("/api/purchases")) {
            return "PURCHASES";
        }

        if (path.startsWith("/api/orders")) {
            return "ORDERS";
        }

        if (path.startsWith("/api/customers")) {
            return "CUSTOMERS";
        }

        if (path.startsWith("/api/suppliers")) {
            return "SUPPLIERS";
        }

        if (path.startsWith("/api/checkout")) {
            return "CHECKOUT";
        }

        return "OTHER";
    }

    private String determineAction(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> "CREATE_OR_PROCESS";
            case "PUT" -> "UPDATE";
            case "PATCH" -> "PARTIAL_UPDATE";
            case "DELETE" -> "DELETE";
            default -> "OTHER";
        };
    }
}