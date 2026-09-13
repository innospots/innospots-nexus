package com.innospots.nexus.service.contract.policy;

import java.time.Duration;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * Resolved operation policy. Empty policy keys mean the capability is not configured.
 *
 * @param permissionKeys      required permission keys
 * @param resourceResolverKey resource resolver key
 * @param rateLimitKey        rate-limit policy key
 * @param bulkheadKey         bulkhead policy key
 * @param circuitKey          circuit-breaker policy key
 * @param timeout             optional timeout bound
 * @param audited             whether audit is enabled
 * @param auditAction         audit action
 * @param auditResourceType   audit resource type
 * @param auditSnapshotKey    snapshot provider key
 * @param auditMode           required or best-effort
 * @param responseProfile     error body profile
 * @author Smars
 * @date 2026/09/13
 * @see OperationDescriptor
 * @see PolicyCatalog
 */
public record OperationPolicy(
        Set<String> permissionKeys,
        String resourceResolverKey,
        String rateLimitKey,
        String bulkheadKey,
        String circuitKey,
        Duration timeout,
        boolean audited,
        String auditAction,
        String auditResourceType,
        String auditSnapshotKey,
        AuditMode auditMode,
        ResponseProfile responseProfile
) {

    public OperationPolicy {
        permissionKeys = permissionKeys == null ? Set.of() : Set.copyOf(permissionKeys);
        resourceResolverKey = blankToNull(resourceResolverKey);
        rateLimitKey = blankToNull(rateLimitKey);
        bulkheadKey = blankToNull(bulkheadKey);
        circuitKey = blankToNull(circuitKey);
        auditAction = blankToNull(auditAction);
        auditResourceType = blankToNull(auditResourceType);
        auditSnapshotKey = blankToNull(auditSnapshotKey);
        if (timeout != null) {
            Checks.isTrue(!timeout.isZero() && !timeout.isNegative(), "timeout must be positive");
        }
        auditMode = auditMode == null ? AuditMode.BEST_EFFORT : auditMode;
        responseProfile = responseProfile == null ? ResponseProfile.LEGACY : responseProfile;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
