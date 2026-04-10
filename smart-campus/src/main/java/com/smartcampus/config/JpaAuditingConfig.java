package com.smartcampus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA Auditing.
 * This ensures @CreatedDate and @LastModifiedDate annotations work seamlessly.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Configuration relies entirely on annotations
}
