package com.innospots.nexus.console.scope.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.scope.domain.model.WorkspaceContext;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceMemberDao;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceEntity;
import com.innospots.nexus.console.scope.workspace.domain.entity.WorkspaceMemberEntity;
import com.innospots.nexus.console.scope.workspace.operator.WorkspaceOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleWorkspaceContextServiceTest {

    private static final String TENANT = "tnt-a";
    private static final String USER = "TUS1";
    private static final String WORKSPACE = "wks-a";

    @Test
    void resolvesWorkspaceWithMemberAndRoleCodes() {
        Harness h = new Harness();
        h.stubWorkspace();
        h.stubMember();
        h.stubRoles();

        WorkspaceContext vo = h.service.resolve(USER, TENANT, WORKSPACE).orElseThrow();

        assertThat(vo.workspaceId()).isEqualTo(WORKSPACE);
        assertThat(vo.tenantId()).isEqualTo(TENANT);
        assertThat(vo.workspaceMemberId()).isEqualTo("wsm-a");
        assertThat(vo.roleCodes()).containsExactly("workspace_admin");
    }

    @Test
    void returnsEmptyWhenWorkspaceNotFound() {
        Harness h = new Harness();
        when(h.workspaceOperator.findById(WORKSPACE)).thenReturn(null);

        assertThat(h.service.resolve(USER, TENANT, WORKSPACE)).isEmpty();
    }

    @Test
    void returnsEmptyWhenWorkspaceBelongsToAnotherTenant() {
        Harness h = new Harness();
        WorkspaceEntity other = workspace();
        other.setTenantId("tnt-other");
        when(h.workspaceOperator.findById(WORKSPACE)).thenReturn(other);

        assertThat(h.service.resolve(USER, TENANT, WORKSPACE)).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserIsNotWorkspaceMember() {
        Harness h = new Harness();
        h.stubWorkspace();
        when(h.workspaceMemberDao.selectOne(any())).thenReturn(null);

        assertThat(h.service.resolve(USER, TENANT, WORKSPACE)).isEmpty();
    }

    @Test
    void returnsEmptyForBlankInput() {
        Harness h = new Harness();

        assertThat(h.service.resolve(null, TENANT, WORKSPACE)).isEmpty();
        assertThat(h.service.resolve(USER, TENANT, " ")).isEmpty();
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

    private static final class Harness {

        private final WorkspaceOperator workspaceOperator = mock(WorkspaceOperator.class);
        private final WorkspaceMemberDao workspaceMemberDao = mock(WorkspaceMemberDao.class);
        private final RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        private final RoleDao roleDao = mock(RoleDao.class);
        private final ConsoleWorkspaceContextService service = new ConsoleWorkspaceContextService(
                workspaceOperator, workspaceMemberDao, roleBindingDao, roleDao);

        private void stubWorkspace() {
            when(workspaceOperator.findById(WORKSPACE)).thenReturn(workspace());
        }

        private void stubMember() {
            WorkspaceMemberEntity member = new WorkspaceMemberEntity();
            member.setWorkspaceMemberId("wsm-a");
            member.setWorkspaceId(WORKSPACE);
            member.setTenantId(TENANT);
            member.setTenantMemberId("tmb-a");
            member.setPortalUserId(USER);
            member.setStatus(BasicStatus.ENABLED.name());
            when(workspaceMemberDao.selectOne(any())).thenReturn(member);
        }

        private void stubRoles() {
            RoleBindingEntity binding = new RoleBindingEntity();
            binding.setRoleId("rol-w");
            when(roleBindingDao.selectList(any())).thenReturn(List.of(binding));
            RoleEntity role = new RoleEntity();
            role.setRoleId("rol-w");
            role.setRoleCode("workspace_admin");
            when(roleDao.selectBatchIds(anyCollection())).thenReturn(List.of(role));
        }
    }
}
