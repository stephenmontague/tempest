package app.tempest.common.security;

/**
 * Security role constants for RBAC enforcement.
 * These roles are expected to be present in the JWT 'roles' claim.
 */
public final class SecurityRoles {

    private SecurityRoles() {
        // Constants class - prevent instantiation
    }

    /**
     * Full access to all operations.
     */
    public static final String ADMIN = "ADMIN";

    /**
     * Order and inventory management capabilities.
     */
    public static final String MANAGER = "MANAGER";

    /**
     * Pick and pack operations.
     */
    public static final String WAREHOUSE_ASSOCIATE = "WAREHOUSE_ASSOCIATE";

    /**
     * API-only access for integrations.
     */
    public static final String INTEGRATION = "INTEGRATION";

    // Role authority prefixes for Spring Security
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_WAREHOUSE_ASSOCIATE = "ROLE_WAREHOUSE_ASSOCIATE";
    public static final String ROLE_INTEGRATION = "ROLE_INTEGRATION";
}

