package com.innospots.nexus.service.contract.policy;

import java.lang.reflect.Type;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;

/**
 * Static operation metadata captured at startup.
 *
 * @param operationId         stable operation identifier
 * @param routeTemplate       route template
 * @param transport           transport name such as http or websocket
 * @param declaredResultType  declared result type
 * @param executionMode       execution mode
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 */
public record OperationDescriptor(
        String operationId,
        String routeTemplate,
        String transport,
        Type declaredResultType,
        ExecutionMode executionMode
) {

    public OperationDescriptor {
        Checks.notBlank(operationId, "operationId");
        Checks.notBlank(transport, "transport");
        Checks.notNull(declaredResultType, "declaredResultType");
        Checks.notNull(executionMode, "executionMode");
        routeTemplate = routeTemplate == null || routeTemplate.isBlank() ? null : routeTemplate;
    }
}
