package com.innospots.nexus.base.thread;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.project.ProjectSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

import java.util.Optional;

/**
 * 基于 {@link TLC} 的用户与作用域快照类型化门面。
 *
 * <p>绑定顺序：用户 → 租户/组织 → 工作区 → 项目（可选）。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see TLC
 * @see UserSnapshot
 * @see TenantSnapshot
 * @see WorkspaceSnapshot
 * @see ProjectSnapshot
 */
public final class SessionContext {

    static final String USER_SNAPSHOT_KEY = "session.userSnapshot";
    static final String TENANT_SNAPSHOT_KEY = "session.tenantSnapshot";
    static final String ORGANIZATION_SNAPSHOT_KEY = "session.organizationSnapshot";
    static final String WORKSPACE_SNAPSHOT_KEY = "session.workspaceSnapshot";
    static final String PROJECT_SNAPSHOT_KEY = "session.projectSnapshot";

    private SessionContext() {
    }

    /**
     * 绑定当前用户快照并同步 TLC 身份键。
     *
     * @param user 用户快照；为 null 时清除用户绑定
     */
    public static void bindUser(UserSnapshot user) {
        if (user == null) {
            clearUser();
            return;
        }
        TLC.put(USER_SNAPSHOT_KEY, user);
        TLC.userId(user.userId());
        TLC.userName(user.userName());
    }

    /**
     * 绑定租户与企业组织档案快照。
     *
     * @param tenant       租户快照
     * @param organization 组织快照
     */
    public static void bindTenant(TenantSnapshot tenant, OrganizationSnapshot organization) {
        Checks.notNull(tenant, "tenant");
        Checks.notNull(organization, "organization");
        Checks.isTrue(tenant.tenantId().equals(organization.tenantId()), "tenant and organization must match");
        TLC.put(TENANT_SNAPSHOT_KEY, tenant);
        TLC.put(ORGANIZATION_SNAPSHOT_KEY, organization);
        TLC.tenantId(tenant.tenantId());
    }

    /**
     * 绑定当前活动工作区快照。
     *
     * @param workspace 工作区快照
     */
    public static void bindWorkspace(WorkspaceSnapshot workspace) {
        Checks.notNull(workspace, "workspace");
        TLC.put(WORKSPACE_SNAPSHOT_KEY, workspace);
        TLC.tenantId(workspace.tenantId());
        TLC.workspaceId(workspace.workspaceId());
    }

    /**
     * 绑定当前活动项目快照；为 null 时清除项目作用域。
     *
     * @param project 项目快照
     */
    public static void bindProject(ProjectSnapshot project) {
        if (project == null) {
            clearProject();
            return;
        }
        TLC.put(PROJECT_SNAPSHOT_KEY, project);
        TLC.tenantId(project.tenantId());
        TLC.workspaceId(project.workspaceId());
        TLC.projectId(project.projectId());
    }

    /**
     * 返回绑定的用户快照，或在可能时从 TLC 重建。
     *
     * @return 用户快照的可选包装
     */
    public static Optional<UserSnapshot> user() {
        Object value = TLC.get(USER_SNAPSHOT_KEY);
        if (value instanceof UserSnapshot snapshot) {
            return Optional.of(snapshot);
        }
        return UserSnapshot.fromContextOptional();
    }

    /**
     * 返回绑定的用户快照，无可用用户时抛出异常。
     *
     * @return 用户快照
     * @throws NexusException 无可用用户时
     */
    public static UserSnapshot requireUser() {
        return user().orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
    }

    /**
     * 返回绑定的租户快照。
     *
     * @return 租户快照的可选包装
     */
    public static Optional<TenantSnapshot> tenant() {
        return snapshot(TENANT_SNAPSHOT_KEY, TenantSnapshot.class);
    }

    /**
     * 返回绑定的组织快照。
     *
     * @return 组织快照的可选包装
     */
    public static Optional<OrganizationSnapshot> organization() {
        return snapshot(ORGANIZATION_SNAPSHOT_KEY, OrganizationSnapshot.class);
    }

    /**
     * 返回绑定的工作区快照。
     *
     * @return 工作区快照的可选包装
     */
    public static Optional<WorkspaceSnapshot> workspace() {
        return snapshot(WORKSPACE_SNAPSHOT_KEY, WorkspaceSnapshot.class);
    }

    /**
     * 返回绑定的项目快照。
     *
     * @return 项目快照的可选包装
     */
    public static Optional<ProjectSnapshot> project() {
        return snapshot(PROJECT_SNAPSHOT_KEY, ProjectSnapshot.class);
    }

    /**
     * 从 TLC 获取当前租户 ID。
     *
     * @return 租户 ID
     */
    public static String tenantId() {
        return TLC.tenantId();
    }

    /**
     * 从 TLC 获取当前工作区 ID。
     *
     * @return 工作区 ID
     */
    public static String workspaceId() {
        return TLC.workspaceId();
    }

    /**
     * 从 TLC 获取当前项目 ID。
     *
     * @return 项目 ID
     */
    public static String projectId() {
        return TLC.projectId();
    }

    /**
     * 获取当前工作区 ID，空白时抛出异常。
     *
     * @return 工作区 ID
     */
    public static String requireWorkspaceId() {
        return Checks.notBlank(TLC.workspaceId(), "workspaceId");
    }

    /**
     * 清除绑定的用户快照及 TLC 身份键。
     */
    public static void clearUser() {
        TLC.remove(USER_SNAPSHOT_KEY);
        TLC.userId(null);
        TLC.userName(null);
    }

    /**
     * 清除租户与组织快照。
     */
    public static void clearTenant() {
        TLC.remove(TENANT_SNAPSHOT_KEY);
        TLC.remove(ORGANIZATION_SNAPSHOT_KEY);
        TLC.tenantId(null);
        TLC.tenantMemberId(null);
    }

    /**
     * 清除工作区快照及 TLC 工作区键。
     */
    public static void clearWorkspace() {
        TLC.remove(WORKSPACE_SNAPSHOT_KEY);
        TLC.workspaceId(null);
    }

    /**
     * 清除项目快照及 TLC 项目键。
     */
    public static void clearProject() {
        TLC.remove(PROJECT_SNAPSHOT_KEY);
        TLC.projectId(null);
    }

    private static <T> Optional<T> snapshot(String key, Class<T> type) {
        Object value = TLC.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }
}
