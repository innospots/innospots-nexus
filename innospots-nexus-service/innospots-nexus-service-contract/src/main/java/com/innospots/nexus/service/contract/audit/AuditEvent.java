package com.innospots.nexus.service.contract.audit;

import java.time.Instant;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;

/**
 * Technical audit event emitted by the service runtime.
 *
 * @param eventId      unique event identifier used for deduplication
 * @param timestamp    event time
 * @param principal    actor
 * @param scope        resource scope
 * @param action       audit action
 * @param resourceType resource type
 * @param resourceId   resource identifier, nullable before create
 * @param requestId    request identifier
 * @param traceId      trace identifier
 * @param result       outcome name
 * @param before       previous snapshot
 * @param after        next snapshot
 * @param details      extra safe fields
 * @author Smars
 * @date 2026/09/13
 * @see AuditStorage
 */
public record AuditEvent(
        String eventId,
        Instant timestamp,
        ServicePrincipal principal,
        ServiceScope scope,
        String action,
        String resourceType,
        String resourceId,
        String requestId,
        String traceId,
        String result,
        Map<String, Object> before,
        Map<String, Object> after,
        Map<String, Object> details
) {

    public AuditEvent {
        Checks.notBlank(eventId, "eventId");
        Checks.notNull(timestamp, "timestamp");
        Checks.notNull(principal, "principal");
        Checks.notNull(scope, "scope");
        Checks.notBlank(action, "action");
        Checks.notBlank(resourceType, "resourceType");
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(traceId, "traceId");
        Checks.notBlank(result, "result");
        resourceId = resourceId == null || resourceId.isBlank() ? null : resourceId;
        before = before == null ? Map.of() : Map.copyOf(before);
        after = after == null ? Map.of() : Map.copyOf(after);
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
