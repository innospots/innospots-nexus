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
 * Typed facade over {@link TLC} for user and scope snapshots.
 *
 * <p>Binding order: user → tenant/organization → workspace → project (optional).</p>
 */
public final class SessionContext {

    static final String USER_SNAPSHOT_KEY = "session.userSnapshot";
    static final String TENANT_SNAPSHOT_KEY = "session.tenantSnapshot";
    static final String ORGANIZATION_SNAPSHOT_KEY = "session.organizationSnapshot";
    static final String WORKSPACE_SNAPSHOT_KEY = "session.workspaceSnapshot";
    static final String PROJECT_SNAPSHOT_KEY = "session.projectSnapshot";

    private SessionContext() {
    }

    /** Binds the current user snapshot and synchronizes TLC identity keys. */
    public static void bindUser(UserSnapshot user) {
        if (user == null) {
            clearUser();
            return;
        }
        TLC.put(USER_SNAPSHOT_KEY, user);
        TLC.userId(user.userId());
        TLC.userName(user.userName());
    }

    /** Binds tenant and business organization profile snapshots. */
    public static void bindTenant(TenantSnapshot tenant, OrganizationSnapshot organization) {
        Checks.notNull(tenant, "tenant");
        Checks.notNull(organization, "organization");
        Checks.isTrue(tenant.tenantId().equals(organization.tenantId()), "tenant and organization must match");
        TLC.put(TENANT_SNAPSHOT_KEY, tenant);
        TLC.put(ORGANIZATION_SNAPSHOT_KEY, organization);
        TLC.tenantId(tenant.tenantId());
    }

    /** Binds the active workspace snapshot. */
    public static void bindWorkspace(WorkspaceSnapshot workspace) {
        Checks.notNull(workspace, "workspace");
        TLC.put(WORKSPACE_SNAPSHOT_KEY, workspace);
        TLC.tenantId(workspace.tenantId());
        TLC.workspaceId(workspace.workspaceId());
    }

    /** Binds the active project snapshot, or clears project scope when null. */
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

    /** Returns the bound user snapshot, or rebuilds one from TLC when possible. */
    public static Optional<UserSnapshot> user() {
        Object value = TLC.get(USER_SNAPSHOT_KEY);
        if (value instanceof UserSnapshot snapshot) {
            return Optional.of(snapshot);
        }
        return UserSnapshot.fromContextOptional();
    }

    /** Returns the bound user snapshot or fails when no user is available. */
    public static UserSnapshot requireUser() {
        return user().orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
    }

    public static Optional<TenantSnapshot> tenant() {
        return snapshot(TENANT_SNAPSHOT_KEY, TenantSnapshot.class);
    }

    public static Optional<OrganizationSnapshot> organization() {
        return snapshot(ORGANIZATION_SNAPSHOT_KEY, OrganizationSnapshot.class);
    }

    public static Optional<WorkspaceSnapshot> workspace() {
        return snapshot(WORKSPACE_SNAPSHOT_KEY, WorkspaceSnapshot.class);
    }

    public static Optional<ProjectSnapshot> project() {
        return snapshot(PROJECT_SNAPSHOT_KEY, ProjectSnapshot.class);
    }

    public static String tenantId() {
        return TLC.tenantId();
    }

    public static String workspaceId() {
        return TLC.workspaceId();
    }

    public static String projectId() {
        return TLC.projectId();
    }

    public static String requireWorkspaceId() {
        return Checks.notBlank(TLC.workspaceId(), "workspaceId");
    }

    /** Clears the bound user snapshot and TLC identity keys. */
    public static void clearUser() {
        TLC.remove(USER_SNAPSHOT_KEY);
        TLC.userId(null);
        TLC.userName(null);
    }

    /** Clears tenant and organization snapshots. */
    public static void clearTenant() {
        TLC.remove(TENANT_SNAPSHOT_KEY);
        TLC.remove(ORGANIZATION_SNAPSHOT_KEY);
        TLC.tenantId(null);
        TLC.tenantMemberId(null);
    }

    /** Clears workspace snapshot and TLC workspace key. */
    public static void clearWorkspace() {
        TLC.remove(WORKSPACE_SNAPSHOT_KEY);
        TLC.workspaceId(null);
    }

    /** Clears project snapshot and TLC project key. */
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
