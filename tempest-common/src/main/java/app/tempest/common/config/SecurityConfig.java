package app.tempest.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security configuration for Tempest services.
 * Currently configured with no authentication for demo/development purposes.
 * Injects a mock JWT with demo tenant and admin role for all requests.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String DEMO_TENANT_ID = "demo-tenant";
    private static final String DEMO_USER_ID = "demo-user";
    private static final List<String> DEMO_ROLES = List.of("ADMIN", "MANAGER", "WAREHOUSE_ASSOCIATE", "INTEGRATION");

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new DemoAuthenticationFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Filter that injects a mock JWT authentication for demo/development mode.
     * This allows controllers that expect JWT authentication to work without
     * an actual OAuth2 provider.
     */
    private static class DemoAuthenticationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            
            // Only inject demo auth if no authentication is already present
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create a mock JWT with demo claims
                Jwt mockJwt = new Jwt(
                        "demo-token",
                        Instant.now(),
                        Instant.now().plusSeconds(3600),
                        Map.of("alg", "none"),
                        Map.of(
                                "sub", DEMO_USER_ID,
                                "tenant_id", DEMO_TENANT_ID,
                                "roles", DEMO_ROLES
                        )
                );

                // Create authorities from roles
                List<SimpleGrantedAuthority> authorities = DEMO_ROLES.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                // Create and set the authentication
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(mockJwt, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }
}
