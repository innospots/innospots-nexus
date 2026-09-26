package com.innospots.nexus.spring.console.jaxrs.support;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;

/**
 * 在关闭请求侧安全时，将配置的 dev 身份绑定到 {@link SessionContext}。
 */
public final class ConsoleDevSessionBinder {

    private ConsoleDevSessionBinder() {
    }

    public static void bind(ConsoleWebProperties.Security.DevSession devSession) {
        if (devSession == null) {
            return;
        }
        String userId = devSession.getUserId();
        UserSnapshot user = UserSnapshot.simple(resolveUserId(userId), userId, null);
        SessionContext.bindUser(user);
        if (devSession.getRealm() != null && !devSession.getRealm().isBlank()) {
            TLC.put(TLC.SECURITY_REALM, devSession.getRealm());
        }
        if (devSession.getTenantMemberId() != null && !devSession.getTenantMemberId().isBlank()) {
            TLC.tenantMemberId(devSession.getTenantMemberId());
        }
        String tenantId = devSession.getTenantId();
        if (tenantId != null && !tenantId.isBlank()) {
            TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
            OrganizationSnapshot organization = new OrganizationSnapshot(
                    tenantId,
                    tenantId,
                    tenantId,
                    "en-US",
                    "USD",
                    null,
                    BasicStatus.ENABLED);
            SessionContext.bindTenant(tenant, organization);
        }
        String workspaceId = devSession.getWorkspaceId();
        if (workspaceId != null && !workspaceId.isBlank()) {
            String boundTenantId = tenantId == null ? "" : tenantId;
            SessionContext.bindWorkspace(new WorkspaceSnapshot(
                    boundTenantId,
                    workspaceId,
                    workspaceId,
                    workspaceId,
                    BasicStatus.ENABLED));
        }
        String projectId = devSession.getProjectId();
        if (projectId != null && !projectId.isBlank()) {
            String boundTenantId = tenantId == null ? "" : tenantId;
            String boundWorkspaceId = workspaceId == null ? "" : workspaceId;
            SessionContext.bindProject(new ProjectSnapshot(
                    boundTenantId,
                    boundWorkspaceId,
                    projectId,
                    projectId,
                    projectId,
                    null,
                    BasicStatus.ENABLED));
        }
    }

    private static Long resolveUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            return (long) userId.hashCode();
        }
    }
}
