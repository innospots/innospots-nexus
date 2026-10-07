package com.innospots.nexus.portal.scope.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.scope.TenantScope;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.portal.auth.api.MembershipDirectory;
import com.innospots.nexus.portal.auth.domain.model.TenantMembership;
import com.innospots.nexus.portal.scope.domain.request.SelectProjectRequest;
import com.innospots.nexus.portal.scope.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.portal.scope.PortalProjectScopeDirectory;
import com.innospots.nexus.portal.scope.PortalTenantScopeDirectory;
import com.innospots.nexus.portal.scope.PortalWorkspaceScopeDirectory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScopeFacadeTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void selectWorkspaceBindsWorkspaceAndIssuesScopedToken() {
        ScopeHarness harness = new ScopeHarness();
        AuthTokenVo token = harness.facade().selectWorkspace(
                "tus-alice", new SelectWorkspaceRequest("tnt-a", "wks-a"));

        assertThat(token.tenantId()).isEqualTo("tnt-a");
        assertThat(token.workspaceId()).isEqualTo("wks-a");
        assertThat(token.projectId()).isNull();
        assertThat(SessionContext.workspaceId()).isEqualTo("wks-a");
        assertThat(SessionContext.tenantId()).isEqualTo("tnt-a");
    }

    @Test
    void selectProjectBindsProjectAndIssuesScopedToken() {
        ScopeHarness harness = new ScopeHarness();
        AuthTokenVo token = harness.facade().selectProject(
                "tus-alice", new SelectProjectRequest("tnt-a", "wks-a", "prj-a"));

        assertThat(token.workspaceId()).isEqualTo("wks-a");
        assertThat(token.projectId()).isEqualTo("prj-a");
        assertThat(SessionContext.projectId()).isEqualTo("prj-a");
    }

    private static final class ScopeHarness {

        private final ScopeFacade facade;

        private ScopeHarness() {
            MembershipDirectory membershipDirectory = tenantUserId ->
                    List.of(new TenantMembership("tnt-a", "tmb-a"));
            PortalTenantScopeDirectory tenantScopeDirectory = mock(PortalTenantScopeDirectory.class);
            PortalWorkspaceScopeDirectory workspaceScopeDirectory = mock(PortalWorkspaceScopeDirectory.class);
            PortalProjectScopeDirectory projectScopeDirectory = mock(PortalProjectScopeDirectory.class);
            stubTenantScope(tenantScopeDirectory);
            when(workspaceScopeDirectory.findWorkspace(eq("tnt-a"), eq("wks-a")))
                    .thenReturn(Optional.of(new WorkspaceSnapshot(
                            "tnt-a", "wks-a", "wks-a", "wks-a", BasicStatus.ENABLED)));
            when(projectScopeDirectory.findProject(eq("tnt-a"), eq("wks-a"), eq("prj-a")))
                    .thenReturn(Optional.of(new ProjectSnapshot(
                            "tnt-a", "wks-a", "prj-a", "prj-a", "prj-a", null, BasicStatus.ENABLED)));

            AuthConfig config = new AuthConfig();
            TokenIssuer issuer = new TokenIssuer(config);
            SessionScopeBinder sessionScopeBinder = new PortalSessionScopeBinder(
                    tenantScopeDirectory, workspaceScopeDirectory, projectScopeDirectory);
            facade = new ScopeFacade(
                    membershipDirectory,
                    workspaceScopeDirectory,
                    projectScopeDirectory,
                    sessionScopeBinder,
                    new AuthTokenPairIssuer(issuer));
        }

        ScopeFacade facade() {
            return facade;
        }

        private static void stubTenantScope(PortalTenantScopeDirectory tenantScopeDirectory) {
            when(tenantScopeDirectory.findByTenantId(eq("tnt-a")))
                    .thenReturn(Optional.of(new TenantScope(
                            new TenantSnapshot("tnt-a", "tnt-a", "tnt-a", BasicStatus.ENABLED),
                            new OrganizationSnapshot(
                                    "tnt-a", "tnt-a", "tnt-a", null, null, null, BasicStatus.ENABLED))));
        }
    }
}
