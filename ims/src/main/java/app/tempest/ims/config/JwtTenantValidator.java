package app.tempest.ims.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/**
 * Custom JWT validator to check that the tenant_id claim is present.
 * Tokens without a tenant_id will be rejected - all operations must be tenant-scoped.
 */
public class JwtTenantValidator implements OAuth2TokenValidator<Jwt> {

    private static final String CLAIM_TENANT_ID = "tenant_id";

    private static final OAuth2Error MISSING_TENANT_ERROR = new OAuth2Error(
            "invalid_token",
            "The required tenant_id claim is missing",
            null
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tenantId = jwt.getClaimAsString(CLAIM_TENANT_ID);
        if (StringUtils.hasText(tenantId)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(MISSING_TENANT_ERROR);
    }
}

