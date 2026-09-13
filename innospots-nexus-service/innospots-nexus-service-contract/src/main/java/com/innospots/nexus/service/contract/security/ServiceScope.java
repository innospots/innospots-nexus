package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * Tenant, workspace, and project scope. Workspace requires tenant; project requires workspace.
 *
 * @param tenantId    tenant identifier, nullable for platform requests
 * @param workspaceId workspace identifier
 * @param projectId   project identifier
 * @author Smars
 * @date 2026/09/13
 * @see ServicePrincipal
 */
public record ServiceScope(String tenantId, String workspaceId, String projectId) {

    public ServiceScope {
        tenantId = blankToNull(tenantId);
        workspaceId = blankToNull(workspaceId);
        projectId = blankToNull(projectId);
        if (workspaceId != null) {
            Checks.notNull(tenantId, "tenantId");
        }
        if (projectId != null) {
            Checks.notNull(workspaceId, "workspaceId");
        }
    }

    /**
     * Returns an empty platform scope.
     *
     * @return platform scope
     */
    public static ServiceScope platform() {
        return new ServiceScope(null, null, null);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
