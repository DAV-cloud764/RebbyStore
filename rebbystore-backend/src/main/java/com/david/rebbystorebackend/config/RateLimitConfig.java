package com.david.rebbystorebackend.config;

import com.david.rebbystorebackend.security.ratelimit.RateLimitBucketService;
import com.david.rebbystorebackend.security.ratelimit.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(
            RateLimitBucketService bucketService,
            RateLimitProperties properties
    ) {
        return new RateLimitFilter(
                bucketService,
                properties
        );
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitFilter rateLimitFilter
    ) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(rateLimitFilter);

        /*
         * Disable automatic servlet-container registration.
         *
         * RateLimitFilter will be executed only through
         * Spring Security's SecurityFilterChain.
         */
        registration.setEnabled(false);

        return registration;
    }
}