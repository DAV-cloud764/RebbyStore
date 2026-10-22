package com.david.rebbystorebackend.config;

import com.david.rebbystorebackend.audit.AuditActivityLogInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditActivityLogConfig implements WebMvcConfigurer {

    private final AuditActivityLogInterceptor auditInterceptor;

    public AuditActivityLogConfig(
            AuditActivityLogInterceptor auditInterceptor
    ) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**");
    }
}