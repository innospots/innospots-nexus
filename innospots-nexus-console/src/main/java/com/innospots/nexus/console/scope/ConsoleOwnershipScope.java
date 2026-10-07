package com.innospots.nexus.console.scope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.persistence.entity.OwnershipEntity;
import com.innospots.nexus.base.util.StringUtils;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 基于 {@code ownerType} / {@code ownerId} / {@code securityRealm} 的查询隔离。
 */
public final class ConsoleOwnershipScope {

    private ConsoleOwnershipScope() {
    }

    /**
     * 从当前会话解析指定层级的归属。
     *
     * @param level 数据默认归属层级
     * @return 归属三元组
     */
    public static ConsoleOwnership requireOwnership(ConsoleOwnershipLevel level) {
        Checks.notNull(level, "level");
        String realm = currentSecurityRealm();
        if (level == ConsoleOwnershipLevel.TENANT) {
            String tenantId = Checks.notBlank(SessionContext.tenantId(), "tenantId");
            return new ConsoleOwnership(RoleOwnerType.TENANT, tenantId, realm);
        }
        String workspaceId = SessionContext.requireWorkspaceId();
        return new ConsoleOwnership(RoleOwnerType.WORKSPACE, workspaceId, realm);
    }

    /**
     * 为审计等场景捕获当前会话归属；无租户时回退为 PLATFORM。
     */
    public static ConsoleOwnership captureForAudit() {
        String realm = currentSecurityRealm();
        String workspaceId = SessionContext.workspaceId();
        if (workspaceId != null && !workspaceId.isBlank()) {
            return new ConsoleOwnership(RoleOwnerType.WORKSPACE, workspaceId, realm);
        }
        String tenantId = SessionContext.tenantId();
        if (tenantId != null && !tenantId.isBlank()) {
            return new ConsoleOwnership(RoleOwnerType.TENANT, tenantId, realm);
        }
        return new ConsoleOwnership(RoleOwnerType.PLATFORM, null, realm);
    }

    /**
     * 将归属条件追加到查询。
     */
    public static <T extends OwnershipEntity> LambdaQueryWrapper<T> apply(
            LambdaQueryWrapper<T> query,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(query, "query");
        Checks.notNull(ownership, "ownership");
        query.eq(T::getOwnerType, ownership.ownerTypeName())
                .eq(T::getSecurityRealm, ownership.securityRealm());
        appendOwnerId(query, ownership);
        return query;
    }

    public static LambdaQueryWrapper<RoleEntity> applyRole(
            LambdaQueryWrapper<RoleEntity> query,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(query, "query");
        Checks.notNull(ownership, "ownership");
        query.eq(RoleEntity::getOwnerType, ownership.ownerTypeName())
                .eq(RoleEntity::getSecurityRealm, ownership.securityRealm());
        appendOwnerIdForRole(query, ownership);
        return query;
    }

    /**
     * 工作区会话下可见角色：同租户 TENANT 级 + 当前 WORKSPACE 级。
     */
    public static LambdaQueryWrapper<RoleEntity> applyRoleListVisibility(
            LambdaQueryWrapper<RoleEntity> query,
            String tenantId,
            String workspaceId,
            String securityRealm
    ) {
        Checks.notNull(query, "query");
        Checks.notBlank(tenantId, "tenantId");
        Checks.notBlank(workspaceId, "workspaceId");
        String realm = StringUtils.defaultIfBlank(securityRealm, "TENANT");
        query.eq(RoleEntity::getSecurityRealm, realm)
                .and(wrapper -> wrapper
                        .nested(tenant -> tenant
                                .eq(RoleEntity::getOwnerType, RoleOwnerType.TENANT.name())
                                .eq(RoleEntity::getOwnerId, tenantId))
                        .or()
                        .nested(workspace -> workspace
                                .eq(RoleEntity::getOwnerType, RoleOwnerType.WORKSPACE.name())
                                .eq(RoleEntity::getOwnerId, workspaceId)));
        return query;
    }

    /**
     * 校验角色在当前工作区会话下可读。
     */
    public static void assertRoleReadable(
            RoleEntity entity,
            String tenantId,
            ConsoleOwnership workspaceOwnership
    ) {
        Checks.notNull(entity, "entity");
        Checks.notBlank(tenantId, "tenantId");
        Checks.notNull(workspaceOwnership, "workspaceOwnership");
        if (!workspaceOwnership.securityRealm().equals(entity.getSecurityRealm())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        if (RoleOwnerType.TENANT.name().equals(entity.getOwnerType())) {
            if (!tenantId.equals(entity.getOwnerId())) {
                throw NexusException.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        if (RoleOwnerType.WORKSPACE.name().equals(entity.getOwnerType())) {
            assertRoleOwnership(entity, workspaceOwnership);
            return;
        }
        throw NexusException.build(NexusStatusCode.NO_PERMISSION);
    }

    /**
     * 校验角色在当前会话下可写（TENANT 或当前 WORKSPACE 归属）。
     */
    public static void assertRoleWritable(
            RoleEntity entity,
            String tenantId,
            ConsoleOwnership workspaceOwnership
    ) {
        assertRoleReadable(entity, tenantId, workspaceOwnership);
    }

    /**
     * 校验实体行与期望归属一致。
     */
    public static void assertOwnership(OwnershipEntity entity, ConsoleOwnership ownership) {
        if (entity == null || ownership == null) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!ownership.ownerTypeName().equals(entity.getOwnerType())
                || !ownership.securityRealm().equals(entity.getSecurityRealm())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        assertOwnerId(entity.getOwnerId(), ownership);
    }

    public static void assertRoleOwnership(RoleEntity entity, ConsoleOwnership ownership) {
        if (entity == null || ownership == null) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!ownership.ownerTypeName().equals(entity.getOwnerType())
                || !ownership.securityRealm().equals(entity.getSecurityRealm())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        assertOwnerId(entity.getOwnerId(), ownership);
    }

    /**
     * 由工作区 ID 构造 WORKSPACE 归属（导航、授权可见性等）。
     */
    public static ConsoleOwnership workspaceOwnership(String workspaceId, String securityRealm) {
        Checks.notBlank(workspaceId, "workspaceId");
        String realm = securityRealm == null || securityRealm.isBlank() ? "TENANT" : securityRealm;
        return new ConsoleOwnership(RoleOwnerType.WORKSPACE, workspaceId, realm);
    }

    public static void stamp(OwnershipEntity entity, ConsoleOwnership ownership) {
        Checks.notNull(entity, "entity");
        Checks.notNull(ownership, "ownership");
        entity.setOwnerType(ownership.ownerTypeName());
        entity.setOwnerId(ownership.ownerType() == RoleOwnerType.PLATFORM ? null : ownership.ownerId());
        entity.setSecurityRealm(ownership.securityRealm());
    }

    private static <T extends OwnershipEntity> void appendOwnerId(
            LambdaQueryWrapper<T> query,
            ConsoleOwnership ownership
    ) {
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            query.and(wrapper -> wrapper.isNull(T::getOwnerId).or().eq(T::getOwnerId, ""));
            return;
        }
        query.eq(T::getOwnerId, ownership.ownerId());
    }

    private static void appendOwnerIdForRole(
            LambdaQueryWrapper<RoleEntity> query,
            ConsoleOwnership ownership
    ) {
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            query.and(wrapper -> wrapper.isNull(RoleEntity::getOwnerId).or().eq(RoleEntity::getOwnerId, ""));
            return;
        }
        query.eq(RoleEntity::getOwnerId, ownership.ownerId());
    }

    private static void assertOwnerId(String entityOwnerId, ConsoleOwnership ownership) {
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            if (entityOwnerId != null && !entityOwnerId.isBlank()) {
                throw NexusException.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        String expectedOwnerId = ownership.ownerId();
        if (expectedOwnerId == null || expectedOwnerId.isBlank()) {
            if (entityOwnerId != null && !entityOwnerId.isBlank()) {
                throw NexusException.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        if (!expectedOwnerId.equals(entityOwnerId)) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private static String currentSecurityRealm() {
        String realm = TLC.securityRealm();
        if (realm == null || realm.isBlank()) {
            return "TENANT";
        }
        return realm;
    }
}
