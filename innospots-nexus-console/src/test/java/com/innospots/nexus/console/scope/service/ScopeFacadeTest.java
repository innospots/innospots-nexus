package com.innospots.nexus.console.scope.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.domain.model.TenantMembership;
import com.innospots.nexus.console.auth.domain.request.SelectProjectRequest;
import com.innospots.nexus.console.auth.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.scope.api.ProjectScopeDirectory;
import com.innospots.nexus.console.scope.api.TenantScopeDirectory;
import com.innospots.nexus.console.scope.api.WorkspaceScopeDirectory;
import com.innospots.nexus.console.scope.domain.model.TenantScope;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

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
            InMemoryScopeDirectory directory = new InMemoryScopeDirectory();
            AuthConfig config = new AuthConfig();
            TokenIssuer issuer = new TokenIssuer(config);
            SessionScopeBinder sessionScopeBinder = new SessionScopeBinder(
                    directory, directory, directory);
            facade = new ScopeFacade(
                    directory,
                    directory,
                    directory,
                    sessionScopeBinder,
                    new AuthTokenPairIssuer(issuer));
        }

        ScopeFacade facade() {
            return facade;
        }
    }

    private static final class InMemoryScopeDirectory
            implements MembershipDirectory, TenantScopeDirectory, WorkspaceScopeDirectory, ProjectScopeDirectory {

        @Override
        public List<TenantMembership> listActiveMemberships(String tenantUserId) {
            return List.of(new TenantMembership("tnt-a", "tmb-a"));
        }

        @Override
        public Optional<TenantScope> findByTenantId(String tenantId) {
            TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
            OrganizationSnapshot organization = new OrganizationSnapshot(
                    tenantId, tenantId, tenantId, null, null, null, BasicStatus.ENABLED);
            return Optional.of(new TenantScope(tenant, organization));
        }

        @Override
        public Optional<WorkspaceSnapshot> findWorkspace(String tenantId, String workspaceId) {
            return Optional.of(new WorkspaceSnapshot(
                    tenantId, workspaceId, workspaceId, workspaceId, BasicStatus.ENABLED));
        }

        @Override
        public Optional<ProjectSnapshot> findProject(String tenantId, String workspaceId, String projectId) {
            return Optional.of(new ProjectSnapshot(
                    tenantId, workspaceId, projectId, projectId, projectId, null, BasicStatus.ENABLED));
        }
    }
}
