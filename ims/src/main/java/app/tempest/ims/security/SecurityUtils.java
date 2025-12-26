package app.tempest.ims.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for extracting security information from JWT tokens.
 * All tenant and user identity MUST be derived from JWT claims, never from request bodies.
 */
public final class SecurityUtils {

    private static final String CLAIM_TENANT_ID = "tenant_id";
    private static final String CLAIM_ROLES = "roles";

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the current JWT from the security context.
     *
     * @return Optional containing the JWT if present
     */
    public static Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    /**
     * Extract tenant ID from JWT claims.
     *
     * @param jwt the JWT token
     * @return Optional containing the tenant ID if present
     */
    public static Optional<String> getTenantId(Jwt jwt) {
        if (jwt == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jwt.getClaimAsString(CLAIM_TENANT_ID));
    }

    /**
     * Extract tenant ID from the current security context.
     *
     * @return Optional containing the tenant ID if present
     */
    public static Optional<String> getCurrentTenantId() {
        return getCurrentJwt().flatMap(SecurityUtils::getTenantId);
    }

    /**
     * Extract tenant ID from JWT, throwing an exception if not present.
     * Use this when tenant ID is required for the operation.
     *
     * @param jwt the JWT token
     * @return the tenant ID
     * @throws MissingTenantException if tenant_id claim is missing
     */
    public static String requireTenantId(Jwt jwt) {
        return getTenantId(jwt)
                .orElseThrow(() -> new MissingTenantException("tenant_id claim is required but missing from JWT"));
    }

    /**
     * Extract tenant ID from the current security context, throwing an exception if not present.
     *
     * @return the tenant ID
     * @throws MissingTenantException if tenant_id claim is missing
     */
    public static String requireCurrentTenantId() {
        return getCurrentJwt()
                .map(SecurityUtils::requireTenantId)
                .orElseThrow(() -> new MissingTenantException("No JWT found in security context"));
    }

    /**
     * Extract user ID (sub claim) from JWT.
     *
     * @param jwt the JWT token
     * @return Optional containing the user ID if present
     */
    public static Optional<String> getUserId(Jwt jwt) {
        if (jwt == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jwt.getSubject());
    }

    /**
     * Extract user ID from the current security context.
     *
     * @return Optional containing the user ID if present
     */
    public static Optional<String> getCurrentUserId() {
        return getCurrentJwt().flatMap(SecurityUtils::getUserId);
    }

    /**
     * Extract user ID from the current security context, throwing an exception if not present.
     *
     * @return the user ID
     * @throws IllegalStateException if user ID is not available
     */
    public static String requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User ID not found in security context"));
    }

    /**
     * Extract roles from JWT claims.
     *
     * @param jwt the JWT token
     * @return List of roles, empty list if none present
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        Object rolesObj = jwt.getClaim(CLAIM_ROLES);
        if (rolesObj instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Extract roles from the current security context.
     *
     * @return List of roles, empty list if none present
     */
    public static List<String> getCurrentRoles() {
        return getCurrentJwt()
                .map(SecurityUtils::getRoles)
                .orElse(Collections.emptyList());
    }

    /**
     * Check if the current user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public static boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }

    /**
     * Check if the current user has any of the specified roles.
     *
     * @param roles the roles to check
     * @return true if the user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        List<String> currentRoles = getCurrentRoles();
        for (String role : roles) {
            if (currentRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}

