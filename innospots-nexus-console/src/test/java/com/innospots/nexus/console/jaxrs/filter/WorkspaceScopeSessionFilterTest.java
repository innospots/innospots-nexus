package com.innospots.nexus.console.jaxrs.filter;

import jakarta.ws.rs.container.ContainerRequestContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceScopeSessionFilterTest {

    private static final String TENANT = "tnt-a";

    @AfterEach
    void clearThreadContext() {
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.clear();
    }

    @Test
    void bindsTenantWorkspaceAndProject() {
        new WorkspaceScopeSessionFilter(request -> scope("prj-a")).filter(request("any"));

        assertThat(SessionContext.tenantId()).isEqualTo(TENANT);
        assertThat(SessionContext.workspaceId()).isEqualTo("wks-a");
        assertThat(SessionContext.projectId()).isEqualTo("prj-a");
        assertThat(SessionContext.tenant()).isPresent();
        assertThat(SessionContext.workspace()).isPresent();
        assertThat(SessionContext.project()).isPresent();
    }

    @Test
    void bindsTenantAndWorkspaceWithoutProject() {
        new WorkspaceScopeSessionFilter(request -> scope(null)).filter(request("any"));

        assertThat(SessionContext.tenant()).isPresent();
        assertThat(SessionContext.workspace()).isPresent();
        assertThat(SessionContext.projectId()).isNull();
        assertThat(SessionContext.project()).isEmpty();
    }

    @Test
    void clearsStaleProjectWhenNoProjectResolved() {
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                TENANT, "wks-old", "old", "旧工作区", BasicStatus.ENABLED));
        SessionContext.bindProject(new ProjectSnapshot(
                TENANT, "wks-old", "prj-old", "old", "旧项目", null, BasicStatus.ENABLED));

        new WorkspaceScopeSessionFilter(request -> scope(null)).filter(request("any"));

        assertThat(SessionContext.projectId()).isNull();
        assertThat(SessionContext.project()).isEmpty();
    }

    @Test
    void skipsBindingWhenResolverReturnsNull() {
        new WorkspaceScopeSessionFilter(request -> null).filter(request("any"));

        assertThat(SessionContext.tenant()).isEmpty();
        assertThat(SessionContext.workspace()).isEmpty();
    }

    @Test
    void skipsBindingWhenResolvedScopeIsInvalid() {
        new WorkspaceScopeSessionFilter(
                request -> new WorkspaceScopeResolver.ResolvedWorkspaceScope(
                        TENANT, "tcode", "租户", " ", "wcode", "工作区", null, null, null, null))
                .filter(request("any"));

        assertThat(SessionContext.workspace()).isEmpty();
    }

    @Test
    void passesCookieHeaderToResolver() {
        StringBuilder captured = new StringBuilder();
        WorkspaceScopeResolver resolver = request -> {
            captured.append(request == null ? "" : request);
            return null;
        };

        new WorkspaceScopeSessionFilter(resolver).filter(request("a=1; b=2"));

        assertThat(captured.toString()).isEqualTo("a=1; b=2");
    }

    private static WorkspaceScopeResolver.ResolvedWorkspaceScope scope(String projectId) {
        return new WorkspaceScopeResolver.ResolvedWorkspaceScope(
                TENANT,
                "tcode-a",
                "租户 A",
                "wks-a",
                "wcode-a",
                "工作区 A",
                "说明 A",
                projectId,
                projectId == null ? null : "pcode-a",
                projectId == null ? null : "项目 A");
    }

    private static ContainerRequestContext request(String cookieHeader) {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("Cookie")).thenReturn(cookieHeader);
        return requestContext;
    }
}
