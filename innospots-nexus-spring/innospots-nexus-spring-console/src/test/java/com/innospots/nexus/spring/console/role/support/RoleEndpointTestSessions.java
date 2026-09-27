package com.innospots.nexus.spring.console.role.support;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;

/**
 * 直调 Endpoint / Operator 时绑定工作区会话（与 HTTP dev-session 配置一致）。
 */
public final class RoleEndpointTestSessions {

    public static final String TENANT_ID = "tnt-it";

    public static final String WORKSPACE_ID = "wks-it";

    private RoleEndpointTestSessions() {
    }

    public static void bindWorkspaceScope() {
        bindWorkspaceScope(TENANT_ID, WORKSPACE_ID);
    }

    public static void bindWorkspaceScope(String tenantId, String workspaceId) {
        TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                tenantId, tenantId, tenantId, "zh-CN", "CNY", null, BasicStatus.ENABLED);
        SessionContext.bindTenant(tenant, organization);
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                tenantId, workspaceId, workspaceId, workspaceId, BasicStatus.ENABLED));
        TLC.put(TLC.SECURITY_REALM, "TENANT");
    }

    public static void clear() {
        TLC.clear();
    }
}
