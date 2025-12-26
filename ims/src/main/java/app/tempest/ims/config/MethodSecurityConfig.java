package app.tempest.ims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables method-level security annotations like @PreAuthorize and @PostAuthorize.
 * This allows fine-grained access control on service and controller methods.
 * 
 * Example usage:
 * <pre>
 * &#64;PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
 * public void updateItem(...) { ... }
 * </pre>
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    // Configuration is handled by annotations
    // prePostEnabled = true enables @PreAuthorize and @PostAuthorize
}

