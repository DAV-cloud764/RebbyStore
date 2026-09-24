package com.david.rebbystorebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.david.rebbystorebackend.config.RateLimitProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RateLimitProperties.class)
public class RebbystoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RebbystoreBackendApplication.class, args);
    }

}
