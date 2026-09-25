package com.innospots.nexus.spring.console.jaxrs.support;

import java.time.Instant;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;

/**
 * 将访问令牌声明绑定到 {@link SessionContext}。
 */
public final class ConsoleTokenSessionBinder {

    private ConsoleTokenSessionBinder() {
    }

    public static void bindAccessToken(TokenClaims claims) {
        if (claims == null) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (!TokenIssuer.PURPOSE_ACCESS.equals(claims.purpose())) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (claims.expiresAt() <= Instant.now().getEpochSecond()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        UserSnapshot user = UserSnapshot.simple(resolveUserId(claims.userId()), claims.userId(), null);
        SessionContext.bindUser(user);
        if (claims.realm() != null) {
            TLC.put(TLC.SECURITY_REALM, claims.realm().name());
        }
        if (claims.tenantMemberId() != null && !claims.tenantMemberId().isBlank()) {
            TLC.tenantMemberId(claims.tenantMemberId());
        }
        if (claims.tenantId() != null && !claims.tenantId().isBlank()) {
            TenantSnapshot tenant = new TenantSnapshot(
                    claims.tenantId(),
                    claims.tenantId(),
                    claims.tenantId(),
                    BasicStatus.ENABLED);
            OrganizationSnapshot organization = new OrganizationSnapshot(
                    claims.tenantId(),
                    claims.tenantId(),
                    claims.tenantId(),
                    "en-US",
                    "USD",
                    null,
                    BasicStatus.ENABLED);
            SessionContext.bindTenant(tenant, organization);
        }
        if (claims.workspaceId() != null && !claims.workspaceId().isBlank()) {
            String tenantId = claims.tenantId() == null ? "" : claims.tenantId();
            SessionContext.bindWorkspace(new WorkspaceSnapshot(
                    tenantId,
                    claims.workspaceId(),
                    claims.workspaceId(),
                    claims.workspaceId(),
                    BasicStatus.ENABLED));
        }
        if (claims.projectId() != null && !claims.projectId().isBlank()) {
            String tenantId = claims.tenantId() == null ? "" : claims.tenantId();
            String workspaceId = claims.workspaceId() == null ? "" : claims.workspaceId();
            SessionContext.bindProject(new ProjectSnapshot(
                    tenantId,
                    workspaceId,
                    claims.projectId(),
                    claims.projectId(),
                    claims.projectId(),
                    null,
                    BasicStatus.ENABLED));
        }
    }

    public static void clear() {
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.clear();
    }

    private static Long resolveUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            return (long) userId.hashCode();
        }
    }
}
