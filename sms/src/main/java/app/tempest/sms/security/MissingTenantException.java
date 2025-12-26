package app.tempest.sms.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a required tenant_id claim is missing from the JWT.
 * This is a non-retryable error - the request should be rejected.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class MissingTenantException extends RuntimeException {

    public MissingTenantException(String message) {
        super(message);
    }

    public MissingTenantException(String message, Throwable cause) {
        super(message, cause);
    }
}

