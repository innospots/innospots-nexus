package com.innospots.nexus.service.contract.context;

import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

/**
 * Immutable per-invocation service context. Every component is required; missing optional
 * capabilities use empty snapshots or unlimited deadlines rather than null.
 *
 * @param requestId    unique request identifier
 * @param request      transport metadata
 * @param security     authenticated or anonymous principal
 * @param scope        tenant/workspace/project scope
 * @param trace        trace snapshot, possibly unsampled
 * @param cancellation cancellation token
 * @param deadline     remaining execution deadline
 * @param attributes   typed attributes
 * @author Smars
 * @date 2026/09/13
 * @see ServiceContextAccessor
 * @see ServicePrincipal
 */
public record ServiceContext(
        String requestId,
        RequestMetadata request,
        ServicePrincipal security,
        ServiceScope scope,
        TraceSnapshot trace,
        CancellationToken cancellation,
        Deadline deadline,
        ContextAttributes attributes
) {

    public ServiceContext {
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(request, "request");
        Checks.notNull(security, "security");
        Checks.notNull(scope, "scope");
        Checks.notNull(trace, "trace");
        Checks.notNull(cancellation, "cancellation");
        Checks.notNull(deadline, "deadline");
        Checks.notNull(attributes, "attributes");
    }

    /**
     * Returns the security principal component.
     *
     * @return principal
     */
    public ServicePrincipal principal() {
        return security;
    }

    /**
     * Returns the optional client identifier from request metadata.
     *
     * @return client id when present
     */
    public Optional<String> clientId() {
        return Optional.ofNullable(request.clientId());
    }

    /**
     * Returns the optional tenant identifier.
     *
     * @return tenant id when present
     */
    public Optional<String> tenantId() {
        return Optional.ofNullable(scope.tenantId());
    }

    /**
     * Returns the optional workspace identifier.
     *
     * @return workspace id when present
     */
    public Optional<String> workspaceId() {
        return Optional.ofNullable(scope.workspaceId());
    }

    /**
     * Returns the optional project identifier.
     *
     * @return project id when present
     */
    public Optional<String> projectId() {
        return Optional.ofNullable(scope.projectId());
    }
}
