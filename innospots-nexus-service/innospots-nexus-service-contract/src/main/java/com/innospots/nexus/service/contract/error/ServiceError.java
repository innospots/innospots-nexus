package com.innospots.nexus.service.contract.error;

import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * Framework-neutral error description for transport mapping.
 *
 * @param code       nine-character status code
 * @param httpStatus transport status
 * @param message    stable bilingual summary
 * @param retryable  whether retry may be appropriate
 * @param details    safe extra fields
 * @author Smars
 * @date 2026/09/13
 * @see ServiceErrorCatalog
 */
public record ServiceError(
        String code,
        int httpStatus,
        String message,
        boolean retryable,
        Map<String, Object> details
) {

    public ServiceError {
        Checks.notBlank(code, "code");
        Checks.notBlank(message, "message");
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
