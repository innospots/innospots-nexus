package com.innospots.nexus.console.scope.service;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeResolver.ResolvedWorkspaceScope;
import com.innospots.nexus.console.scope.domain.model.TenantContext;
import com.innospots.nexus.console.scope.domain.model.WorkspaceContext;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceEntity;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleWorkspaceScopeResolverTest {

    private static final ConsoleWorkspaceScopeResolver.Names NAMES =
            new ConsoleWorkspaceScopeResolver.Names("tid", "wid", "pid");
    private static final String TENANT = "tnt-a";
    private static final String USER = "TUS1";
    private static final String WORKSPACE = "wks-a";
    private static final String PROJECT = "prj-a";

    @AfterEach
    void clearThreadContext() {
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.clear();
    }

    @Test
    void resolvesTenantAndWorkspaceFromCookies() {
        Harness h = new Harness();
        h.stubTenantOk();
        h.stubWorkspaceOk();

        ResolvedWorkspaceScope scope = h.resolver().resolve(cookies(TENANT, WORKSPACE, null));

        assertThat(scope).isNotNull();
        assertThat(scope.tenantId()).isEqualTo(TENANT);
        assertThat(scope.tenantName()).isEqualTo("租户 A");
        assertThat(scope.workspaceId()).isEqualTo(WORKSPACE);
        assertThat(scope.workspaceName()).isEqualTo("工作区 A");
        assertThat(scope.projectId()).isNull();
    }

    @Test
    void resolvesProjectWhenDeclared() {
        Harness h = new Harness();
        h.stubTenantOk();
        h.stubWorkspaceOk();
        when(h.projectLookup.find(TENANT, WORKSPACE, PROJECT))
                .thenReturn(Optional.of(new com.innospots.nexus.base.domain.project.ProjectSnapshot(
                        TENANT, WORKSPACE, PROJECT, "pcode", "项目 A", null, BasicStatus.ENABLED)));

        ResolvedWorkspaceScope scope = h.resolver().resolve(cookies(TENANT, WORKSPACE, PROJECT));

        assertThat(scope).isNotNull();
        assertThat(scope.projectId()).isEqualTo(PROJECT);
        assertThat(scope.projectName()).isEqualTo("项目 A");
    }

    @Test
    void rejectsProjectFromAnotherWorkspace() {
        Harness h = new Harness();
        h.stubTenantOk();
        h.stubWorkspaceOk();
        when(h.projectLookup.find(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, PROJECT))).isNull();
    }

    @Test
    void skipsPlatformRealm() {
        Harness h = new Harness();
        h.stubTenantOk();
        h.stubWorkspaceOk();
        TLC.securityRealm(SecurityRealm.PLATFORM.name());

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, null))).isNull();
    }

    @Test
    void returnsNullWhenTenantCookieMissing() {
        Harness h = new Harness();
        h.stubWorkspaceOk();

        assertThat(h.resolver().resolve(cookies(null, WORKSPACE, null))).isNull();
    }

    @Test
    void returnsNullWhenWorkspaceCookieMissing() {
        Harness h = new Harness();
        h.stubTenantOk();

        assertThat(h.resolver().resolve(cookies(TENANT, null, null))).isNull();
    }

    @Test
    void returnsNullWhenUserIsNotTenantMember() {
        Harness h = new Harness();
        when(h.tenantContextService.resolve(USER, TENANT)).thenReturn(Optional.empty());

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, null))).isNull();
    }

    @Test
    void returnsNullWhenUserIsNotWorkspaceMember() {
        Harness h = new Harness();
        h.stubTenantOk();
        when(h.workspaceContextService.resolve(USER, TENANT, WORKSPACE))
                .thenReturn(Optional.empty());

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, null))).isNull();
    }

    @Test
    void returnsNullWhenWorkspaceDisabled() {
        Harness h = new Harness();
        h.stubTenantOk();
        WorkspaceEntity disabled = workspace();
        disabled.setStatus(BasicStatus.DISABLED.name());
        when(h.workspaceContextService.resolve(USER, TENANT, WORKSPACE))
                .thenReturn(Optional.of(new WorkspaceContext(disabled, member(), java.util.List.of())));

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, null))).isNull();
    }

    @Test
    void returnsNullWhenUserIdMissing() {
        Harness h = new Harness();
        TLC.remove(TLC.USER_ID);

        assertThat(h.resolver().resolve(cookies(TENANT, WORKSPACE, null))).isNull();
    }

    private static String cookies(String tenantId, String workspaceId, String projectId) {
        StringBuilder sb = new StringBuilder();
        if (tenantId != null) {
            sb.append(NAMES.tenantId()).append('=').append(tenantId);
        }
        if (workspaceId != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(NAMES.workspaceId()).append('=').append(workspaceId);
        }
        if (projectId != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(NAMES.projectId()).append('=').append(projectId);
        }
        return sb.toString();
    }

    private static WorkspaceEntity workspace() {
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setTenantId(TENANT);
        entity.setWorkspaceId(WORKSPACE);
        entity.setWorkspaceCode("wcode");
        entity.setWorkspaceName("工作区 A");
        entity.setStatus(BasicStatus.ENABLED.name());
        return entity;
    }

    private static WorkspaceMemberEntity member() {
        WorkspaceMemberEntity entity = new WorkspaceMemberEntity();
        entity.setWorkspaceId(WORKSPACE);
        entity.setTenantId(TENANT);
        entity.setTenantMemberId("tmb-a");
        entity.setPortalUserId(USER);
        entity.setStatus(BasicStatus.ENABLED.name());
        return entity;
    }

    private static final class Harness {

        private final ConsoleTenantContextService tenantContextService = mock(ConsoleTenantContextService.class);
        private final ConsoleWorkspaceContextService workspaceContextService =
                mock(ConsoleWorkspaceContextService.class);
        private final ConsoleWorkspaceScopeResolver.ConsoleProjectLookup projectLookup =
                mock(ConsoleWorkspaceScopeResolver.ConsoleProjectLookup.class);

        private Harness() {
            TLC.userId(USER);
        }

        private void stubTenantOk() {
            TenantEntity tenant = new TenantEntity();
            tenant.setTenantId(TENANT);
            tenant.setTenantCode("tcode");
            tenant.setTenantName("租户 A");
            tenant.setStatus("ACTIVE");
            TenantMemberEntity member = new TenantMemberEntity();
            member.setTenantId(TENANT);
            member.setTenantMemberId("tmb-a");
            member.setPortalUserId(USER);
            when(tenantContextService.resolve(USER, TENANT))
                    .thenReturn(Optional.of(new TenantContext(
                            tenant, member, java.util.List.of("tenant_admin"))));
        }

        private void stubWorkspaceOk() {
            when(workspaceContextService.resolve(eq(USER), eq(TENANT), eq(WORKSPACE)))
                    .thenReturn(Optional.of(new WorkspaceContext(
                            workspace(), member(), java.util.List.of("workspace_admin"))));
        }

        private ConsoleWorkspaceScopeResolver resolver() {
            return new ConsoleWorkspaceScopeResolver(
                    tenantContextService, workspaceContextService, projectLookup, NAMES);
        }
    }
}
