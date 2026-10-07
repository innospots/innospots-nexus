package com.innospots.nexus.core.persistence.scope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 基于 {@link OwnershipEntity} 的查询隔离与归属写入。
 */
public final class OwnershipScope {

    private OwnershipScope() {
    }

    /**
     * 从当前会话解析资源默认归属（工作区优先，其次租户，否则 PLATFORM）。
     */
    public static PersistenceOwnership captureFromSession() {
        String realm = currentSecurityRealm();
        String workspaceId = SessionContext.workspaceId();
        if (workspaceId != null && !workspaceId.isBlank()) {
            return new PersistenceOwnership(OwnerType.WORKSPACE, workspaceId, realm);
        }
        String tenantId = SessionContext.tenantId();
        if (tenantId != null && !tenantId.isBlank()) {
            return new PersistenceOwnership(OwnerType.TENANT, tenantId, realm);
        }
        return new PersistenceOwnership(OwnerType.PLATFORM, null, realm);
    }

    public static void stamp(OwnershipEntity entity, PersistenceOwnership ownership) {
        Checks.notNull(entity, "entity");
        Checks.notNull(ownership, "ownership");
        entity.setOwnerType(ownership.ownerTypeName());
        entity.setOwnerId(ownership.ownerType() == OwnerType.PLATFORM ? null : ownership.ownerId());
        entity.setSecurityRealm(ownership.securityRealm());
    }

    public static <T extends OwnershipEntity> LambdaQueryWrapper<T> apply(
            LambdaQueryWrapper<T> query,
            PersistenceOwnership ownership) {
        Checks.notNull(query, "query");
        Checks.notNull(ownership, "ownership");
        query.eq(T::getOwnerType, ownership.ownerTypeName())
                .eq(T::getSecurityRealm, ownership.securityRealm());
        appendOwnerId(query, ownership, T::getOwnerId);
        return query;
    }

    public static void assertOwnership(OwnershipEntity entity, PersistenceOwnership ownership) {
        if (entity == null || ownership == null) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!ownership.ownerTypeName().equals(entity.getOwnerType())
                || !ownership.securityRealm().equals(entity.getSecurityRealm())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        assertOwnerId(entity.getOwnerId(), ownership);
    }

    private static <T extends OwnershipEntity> void appendOwnerId(
            LambdaQueryWrapper<T> query,
            PersistenceOwnership ownership,
            SFunction<T, String> ownerIdColumn) {
        if (ownership.ownerType() == OwnerType.PLATFORM) {
            query.and(wrapper -> wrapper.isNull(ownerIdColumn).or().eq(ownerIdColumn, ""));
            return;
        }
        query.eq(ownerIdColumn, ownership.ownerId());
    }

    private static void assertOwnerId(String entityOwnerId, PersistenceOwnership ownership) {
        if (ownership.ownerType() == OwnerType.PLATFORM) {
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
