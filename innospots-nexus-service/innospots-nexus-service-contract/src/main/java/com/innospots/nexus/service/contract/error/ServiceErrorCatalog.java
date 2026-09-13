package com.innospots.nexus.service.contract.error;

import java.util.LinkedHashMap;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * Resolves registered status catalogs into {@link ServiceError} values.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceError
 * @see ServiceStatusCode
 */
public final class ServiceErrorCatalog {

    private final Map<String, StatusCode> codes;

    private ServiceErrorCatalog(Map<String, StatusCode> codes) {
        this.codes = Map.copyOf(codes);
    }

    /**
     * Builds the default catalog from platform and service status enums.
     *
     * @return catalog covering NEX and SRV codes
     */
    public static ServiceErrorCatalog standard() {
        return of(concat(NexusStatusCode.values(), ServiceStatusCode.values()));
    }

    /**
     * Builds a catalog from the given status codes.
     *
     * @param statuses codes to register
     * @return catalog
     */
    public static ServiceErrorCatalog of(StatusCode... statuses) {
        Checks.notNull(statuses, "statuses");
        Map<String, StatusCode> codes = new LinkedHashMap<>();
        for (StatusCode status : statuses) {
            String fullCode = status.fullCode();
            if (codes.containsKey(fullCode)) {
                throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
            }
            codes.put(fullCode, status);
        }
        return new ServiceErrorCatalog(codes);
    }

    /**
     * Resolves a full code to a safe error. Unknown codes map to {@link NexusStatusCode#SYSTEM_ERROR}.
     *
     * @param fullCode machine-readable code
     * @return error description
     */
    public ServiceError resolve(String fullCode) {
        StatusCode status = codes.get(fullCode);
        if (status == null) {
            status = NexusStatusCode.SYSTEM_ERROR;
        }
        return new ServiceError(
                status.fullCode(),
                status.httpStatusCode(),
                status.summary(),
                retryable(status),
                Map.of());
    }

    private static boolean retryable(StatusCode status) {
        if (status instanceof ServiceStatusCode serviceStatus) {
            return serviceStatus.retryable();
        }
        return status == NexusStatusCode.LIMIT_EXCEEDED;
    }

    private static StatusCode[] concat(StatusCode[] first, StatusCode[] second) {
        StatusCode[] merged = new StatusCode[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
