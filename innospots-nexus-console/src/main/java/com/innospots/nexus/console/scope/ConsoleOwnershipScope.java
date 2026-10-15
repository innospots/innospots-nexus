package com.innospots.nexus.console.scope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.persistence.entity.OwnershipEntity;
import com.innospots.nexus.base.util.StringUtils;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 {@code ownerType} / {@code ownerId} / {@code securityRealm} 的查询隔离。
 */
public final class ConsoleOwnershipScope {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleOwnershipScope.class);

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
        if (level == ConsoleOwnershipLevel.PLATFORM) {
            if (!SecurityRealm.PLATFORM.name().equals(realm)) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return new ConsoleOwnership(RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());
        }
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
     *
     * <p>使用 {@code apply} SQL 片段而非 {@code T::getXxx}：父类 {@link OwnershipEntity}
     * 上的方法引用会使 MyBatis-Plus 按抽象父类建 lambda 缓存并失败。</p>
     */
    public static <T extends OwnershipEntity> LambdaQueryWrapper<T> apply(
            LambdaQueryWrapper<T> query,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(query, "query");
        Checks.notNull(ownership, "ownership");
        query.apply("owner_type = {0}", ownership.ownerTypeName())
                .apply("security_realm = {0}", ownership.securityRealm());
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
     * 按会话归属限定可见角色：平台会话仅 PLATFORM 行；租户会话仅当前租户 TENANT 行；
     * 工作区会话为同租户 TENANT 与当前 WORKSPACE 行。
     */
    public static LambdaQueryWrapper<RoleEntity> applyRoleListVisibility(
            LambdaQueryWrapper<RoleEntity> query,
            ConsoleOwnership sessionOwnership,
            String tenantId,
            String workspaceId
    ) {
        Checks.notNull(query, "query");
        Checks.notNull(sessionOwnership, "sessionOwnership");
        if (sessionOwnership.ownerType() == RoleOwnerType.PLATFORM) {
            return applyPlatformRoleListVisibility(query);
        }
        Checks.notBlank(tenantId, "tenantId");
        String realm = StringUtils.defaultIfBlank(sessionOwnership.securityRealm(), SecurityRealm.TENANT.name());
        if (sessionOwnership.ownerType() == RoleOwnerType.TENANT) {
            query.eq(RoleEntity::getSecurityRealm, realm)
                    .eq(RoleEntity::getOwnerType, RoleOwnerType.TENANT.name())
                    .eq(RoleEntity::getOwnerId, tenantId);
        } else if (sessionOwnership.ownerType() == RoleOwnerType.WORKSPACE) {
            Checks.notBlank(workspaceId, "workspaceId");
            query.eq(RoleEntity::getSecurityRealm, realm)
                    .and(wrapper -> wrapper
                            .nested(tenant -> tenant
                                    .eq(RoleEntity::getOwnerType, RoleOwnerType.TENANT.name())
                                    .eq(RoleEntity::getOwnerId, tenantId))
                            .or()
                            .nested(workspace -> workspace
                                    .eq(RoleEntity::getOwnerType, RoleOwnerType.WORKSPACE.name())
                                    .eq(RoleEntity::getOwnerId, workspaceId)));
        } else {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "applyRoleListVisibility sessionOwnerType={} tenantId={} workspaceId={} securityRealm={}"
                            + " sqlSegment={}",
                    sessionOwnership.ownerType(),
                    tenantId,
                    workspaceId,
                    realm,
                    query.getCustomSqlSegment());
        }
        return query;
    }

    /**
     * 兼容仍按字符串安全域调用的场景；PLATFORM 域忽略 tenantId / workspaceId。
     */
    public static LambdaQueryWrapper<RoleEntity> applyRoleListVisibility(
            LambdaQueryWrapper<RoleEntity> query,
            String tenantId,
            String workspaceId,
            String securityRealm
    ) {
        Checks.notNull(query, "query");
        String realm = StringUtils.defaultIfBlank(securityRealm, SecurityRealm.TENANT.name());
        if (SecurityRealm.PLATFORM.name().equals(realm)) {
            return applyPlatformRoleListVisibility(query);
        }
        Checks.notBlank(tenantId, "tenantId");
        if (workspaceId == null || workspaceId.isBlank()) {
            ConsoleOwnership tenantOwnership = new ConsoleOwnership(
                    RoleOwnerType.TENANT,
                    tenantId,
                    realm);
            return applyRoleListVisibility(query, tenantOwnership, tenantId, null);
        }
        ConsoleOwnership workspaceOwnership = workspaceOwnership(workspaceId, realm);
        return applyRoleListVisibility(query, workspaceOwnership, tenantId, workspaceId);
    }

    private static LambdaQueryWrapper<RoleEntity> applyPlatformRoleListVisibility(
            LambdaQueryWrapper<RoleEntity> query
    ) {
        query.eq(RoleEntity::getSecurityRealm, SecurityRealm.PLATFORM.name())
                .eq(RoleEntity::getOwnerType, RoleOwnerType.PLATFORM.name())
                .and(owner -> owner.isNull(RoleEntity::getOwnerId).or().eq(RoleEntity::getOwnerId, ""));
        if (LOG.isDebugEnabled()) {
            LOG.debug("applyPlatformRoleListVisibility sqlSegment={}", query.getCustomSqlSegment());
        }
        return query;
    }

    /**
     * 校验角色在当前工作区会话下可读。
     */
    public static void assertRoleReadable(
            RoleEntity entity,
            String tenantId,
            ConsoleOwnership sessionOwnership
    ) {
        Checks.notNull(entity, "entity");
        Checks.notNull(sessionOwnership, "sessionOwnership");
        if (!sessionOwnership.securityRealm().equals(entity.getSecurityRealm())) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (sessionOwnership.ownerType() == RoleOwnerType.PLATFORM) {
            if (!RoleOwnerType.PLATFORM.name().equals(entity.getOwnerType())) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            if (entity.getOwnerId() != null && !entity.getOwnerId().isBlank()) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        Checks.notBlank(tenantId, "tenantId");
        if (sessionOwnership.ownerType() == RoleOwnerType.TENANT) {
            if (!RoleOwnerType.TENANT.name().equals(entity.getOwnerType())) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            if (!tenantId.equals(entity.getOwnerId())) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        if (RoleOwnerType.TENANT.name().equals(entity.getOwnerType())) {
            if (!tenantId.equals(entity.getOwnerId())) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        if (RoleOwnerType.WORKSPACE.name().equals(entity.getOwnerType())) {
            assertRoleOwnership(entity, sessionOwnership);
            return;
        }
        throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
    }

    /**
     * 按当前控制台会话层级解析角色 API 使用的归属三元组。
     */
    public static ConsoleOwnership requireRoleSessionOwnership() {
        ConsoleSessionScope sessionScope = ConsoleSessionContext.scope();
        if (sessionScope == ConsoleSessionScope.PLATFORM) {
            return requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        }
        String tenantId = Checks.notBlank(SessionContext.tenantId(), "tenantId");
        if (sessionScope == ConsoleSessionScope.TENANT) {
            String realm = currentSecurityRealm();
            if (SecurityRealm.PLATFORM.name().equals(realm)) {
                realm = SecurityRealm.TENANT.name();
            }
            return new ConsoleOwnership(RoleOwnerType.TENANT, tenantId, realm);
        }
        return requireOwnership(ConsoleOwnershipLevel.WORKSPACE);
    }

    /**
     * 校验角色在当前会话下可写（TENANT 或当前 WORKSPACE 归属）。
     */
    public static void assertRoleWritable(
            RoleEntity entity,
            String tenantId,
            ConsoleOwnership sessionOwnership
    ) {
        assertRoleReadable(entity, tenantId, sessionOwnership);
    }

    /**
     * 校验实体行与期望归属一致。
     */
    public static void assertOwnership(OwnershipEntity entity, ConsoleOwnership ownership) {
        if (entity == null || ownership == null) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!ownership.ownerTypeName().equals(entity.getOwnerType())
                || !ownership.securityRealm().equals(entity.getSecurityRealm())) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        assertOwnerId(entity.getOwnerId(), ownership);
    }

    public static void assertRoleOwnership(RoleEntity entity, ConsoleOwnership ownership) {
        if (entity == null || ownership == null) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!ownership.ownerTypeName().equals(entity.getOwnerType())
                || !ownership.securityRealm().equals(entity.getSecurityRealm())) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
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
            query.apply("(owner_id IS NULL OR owner_id = {0})", "");
            return;
        }
        query.apply("owner_id = {0}", ownership.ownerId());
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
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        String expectedOwnerId = ownership.ownerId();
        if (expectedOwnerId == null || expectedOwnerId.isBlank()) {
            if (entityOwnerId != null && !entityOwnerId.isBlank()) {
                throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
            }
            return;
        }
        if (!expectedOwnerId.equals(entityOwnerId)) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private static String currentSecurityRealm() {
        String realm = TLC.securityRealm();
        if (realm == null || realm.isBlank()) {
            return SecurityRealm.TENANT.name();
        }
        return realm;
    }
}
