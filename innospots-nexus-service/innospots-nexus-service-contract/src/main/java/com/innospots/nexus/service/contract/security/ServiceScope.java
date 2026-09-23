package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * 租户、工作区与项目作用域。工作区需要租户；项目需要工作区。
 *
 * @param tenantId    租户标识，平台请求可为空
 * @param workspaceId 工作区标识
 * @param projectId   项目标识
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
     * 返回空平台作用域。
     *
     * @return 平台作用域
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
