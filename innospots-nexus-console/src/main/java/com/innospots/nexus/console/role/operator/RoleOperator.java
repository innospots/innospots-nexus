package com.innospots.nexus.console.role.operator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

/**
 * 工作空间作用域内角色持久化与查询，可供子类扩展过滤或校验规则。
 */
@Slf4j
@RequiredArgsConstructor
public class RoleOperator {

    private static final int DEFAULT_ROLE_SORT_ORDER = 1;

    private final RoleDao roleDao;
    private final RoleBindingDao roleBindingDao;
    private final RoleConverter roleConverter;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        RoleSessionScope scope = requireRoleSessionScope();
        RolePageRequest pageRequest = request == null ? new RolePageRequest() : request;
        if (log.isDebugEnabled()) {
            log.debug(
                    "pageRoles session tenantId={} workspaceId={} securityRealm={} filters input={} status={}"
                            + " builtIn={} pageNo={} pageSize={}",
                    scope.tenantId(),
                    scope.workspaceId(),
                    scope.securityRealm(),
                    pageRequest.input(),
                    pageRequest.status(),
                    pageRequest.builtIn(),
                    pageRequest.pageNo(),
                    pageRequest.pageSize());
        }
        LambdaQueryWrapper<RoleEntity> query = pageQuery(pageRequest, scope);
        if (log.isDebugEnabled()) {
            log.debug("pageRoles query sqlSegment={}", query.getCustomSqlSegment());
        }
        IPage<RoleEntity> selectedPage = roleDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        if (log.isDebugEnabled()) {
            log.debug("pageRoles result total={} returned={}", selectedPage.getTotal(), selectedPage.getRecords().size());
        }
        List<RoleVo> records = selectedPage.getRecords().stream()
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.sessionOwnership())))
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public RoleVo getRole(String roleId) {
        RoleSessionScope scope = requireRoleSessionScope();
        return findReadableEntity(roleId, scope)
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.sessionOwnership())))
                .orElseThrow(() -> ConsoleExceptions.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    @Transactional
    public RoleVo createRole(RoleCreateRequest request) {
        Objects.requireNonNull(request, "request");
        RoleSessionScope scope = requireRoleSessionScope();
        RoleOwnerType ownerType = request.ownerType();
        if (ownerType == null) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        String ownerId = resolveOwnerId(ownerType, request.ownerId(), scope);
        validateOwner(ownerType, ownerId);
        String securityRealm = ownerType.securityRealm().name();
        if (log.isDebugEnabled()) {
            log.debug(
                    "createRole session tenantId={} workspaceId={} securityRealm={} ownerType={}"
                            + " requestOwnerId={} resolvedOwnerId={} resolvedSecurityRealm={} roleCode={}",
                    scope.tenantId(),
                    scope.workspaceId(),
                    scope.securityRealm(),
                    ownerType,
                    request.ownerId(),
                    ownerId,
                    securityRealm,
                    request.roleCode());
        }
        if (existsRoleCode(ownerType, ownerId, securityRealm, request.roleCode())) {
            throw ConsoleExceptions.build(RoleStatusCode.ROLE_CODE_DUPLICATED);
        }
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(request.roleName());
        entity.setRoleCode(request.roleCode());
        entity.setOwnerType(ownerType.name());
        entity.setOwnerId(normalizeOwnerId(ownerType, ownerId));
        entity.setSecurityRealm(securityRealm);
        entity.setDescription(request.description());
        entity.setSortOrder(DEFAULT_ROLE_SORT_ORDER);
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(false);
        entity.setAdministrator(false);
        roleDao.insert(entity);
        log.info("Created role {} with code {}", entity.getRoleId(), entity.getRoleCode());
        return roleConverter.toVo(entity, 0L);
    }

    @Transactional
    public RoleVo updateRole(String roleId, RoleUpdateRequest request) {
        RoleSessionScope scope = requireRoleSessionScope();
        Objects.requireNonNull(request, "request");
        RoleEntity entity = requireWritableEntity(roleId, scope);
        if (request.roleName() != null) {
            entity.setRoleName(request.roleName());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
        roleDao.updateById(entity);
        log.info("Updated role {}", entity.getRoleId());
        return roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.sessionOwnership()));
    }

    @Transactional
    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        RoleSessionScope scope = requireRoleSessionScope();
        Objects.requireNonNull(request, "request");
        if (request.status() == null) {
            throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
        }
        RoleEntity entity = requireWritableEntity(roleId, scope);
        entity.setStatus(roleConverter.formatStatus(request.status()));
        roleDao.updateById(entity);
        log.info("Updated role {} status to {}", entity.getRoleId(), request.status());
    }

    @Transactional
    public void deleteRole(String roleId) {
        RoleSessionScope scope = requireRoleSessionScope();
        RoleEntity entity = requireWritableEntity(roleId, scope);
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw ConsoleExceptions.build(RoleStatusCode.ROLE_PROTECTED);
        }
        roleBindingDao.delete(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>().eq(RoleBindingEntity::getRoleId, roleId),
                scope.sessionOwnership()));
        roleDao.deleteById(roleId);
        log.info("Deleted role {}", roleId);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        RoleSessionScope scope = requireRoleSessionScope();
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSortOrder)
                        .orderByAsc(RoleEntity::getRoleName),
                scope.sessionOwnership(),
                scope.tenantId(),
                scope.workspaceId());
        if (status != null) {
            query.eq(RoleEntity::getStatus, status.name());
        }
        return roleDao.selectList(query).stream()
                .map(roleConverter::toOption)
                .toList();
    }

    protected RoleEntity requireEntity(String roleId, ConsoleOwnership ownership) {
        RoleSessionScope scope = requireRoleSessionScope();
        if (!scope.sessionOwnership().equals(ownership)) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        return requireReadableEntity(roleId, scope);
    }

    protected RoleEntity requireReadableEntity(String roleId, RoleSessionScope scope) {
        return findReadableEntity(roleId, scope)
                .orElseThrow(() -> ConsoleExceptions.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    protected RoleEntity requireWritableEntity(String roleId, RoleSessionScope scope) {
        RoleEntity entity = requireReadableEntity(roleId, scope);
        ConsoleOwnershipScope.assertRoleWritable(entity, scope.tenantId(), scope.sessionOwnership());
        return entity;
    }

    protected Optional<RoleEntity> findReadableEntity(String roleId, RoleSessionScope scope) {
        if (roleId == null || roleId.isBlank()) {
            return Optional.empty();
        }
        RoleEntity entity = roleDao.selectOne(ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getRoleId, roleId),
                scope.sessionOwnership(),
                scope.tenantId(),
                scope.workspaceId()));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertRoleReadable(entity, scope.tenantId(), scope.sessionOwnership());
        return Optional.of(entity);
    }

    protected long countMembers(String roleId, ConsoleOwnership ownership) {
        return roleBindingDao.selectCount(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>().eq(RoleBindingEntity::getRoleId, roleId),
                ownership));
    }

    private boolean existsRoleCode(
            RoleOwnerType ownerType,
            String ownerId,
            String securityRealm,
            String roleCode
    ) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        String normalizedOwnerId = normalizeOwnerId(ownerType, ownerId);
        LambdaQueryWrapper<RoleEntity> query = new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getOwnerType, ownerType.name())
                .eq(RoleEntity::getSecurityRealm, securityRealm)
                .eq(RoleEntity::getRoleCode, roleCode);
        if (normalizedOwnerId == null) {
            query.isNull(RoleEntity::getOwnerId);
        } else {
            query.eq(RoleEntity::getOwnerId, normalizedOwnerId);
        }
        return roleDao.selectCount(query) > 0;
    }

    protected LambdaQueryWrapper<RoleEntity> pageQuery(RolePageRequest request, RoleSessionScope scope) {
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSortOrder)
                        .orderByAsc(RoleEntity::getRoleName),
                scope.sessionOwnership(),
                scope.tenantId(),
                scope.workspaceId());
        if (hasText(request.input())) {
            String input = request.input().trim();
            query.and(wrapper -> wrapper
                    .like(RoleEntity::getRoleName, input)
                    .or()
                    .like(RoleEntity::getRoleCode, input));
        }
        if (request.status() != null) {
            query.eq(RoleEntity::getStatus, request.status().name());
        }
        if (request.builtIn() != null) {
            query.eq(RoleEntity::getBuiltIn, request.builtIn());
        }
        return query;
    }

    protected static void validateOwner(RoleOwnerType ownerType, String ownerId) {
        if (ownerType == null) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        if (ownerType == RoleOwnerType.PLATFORM) {
            return;
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
    }

    protected static String normalizeOwnerId(RoleOwnerType ownerType, String ownerId) {
        if (ownerType == RoleOwnerType.PLATFORM) {
            return null;
        }
        return ownerId;
    }

    /**
     * 请求未显式携带 ownerId 时，按归属层级从当前会话补全。
     */
    protected static String resolveOwnerId(
            RoleOwnerType ownerType,
            String requestedOwnerId,
            RoleSessionScope scope
    ) {
        if (ownerType == RoleOwnerType.PLATFORM) {
            if (hasText(requestedOwnerId)) {
                throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
            }
            return null;
        }
        if (hasText(requestedOwnerId)) {
            return requestedOwnerId.trim();
        }
        if (ownerType == RoleOwnerType.TENANT) {
            return scope.tenantId();
        }
        if (ownerType == RoleOwnerType.WORKSPACE) {
            if (!hasText(scope.workspaceId())) {
                throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
            }
            return scope.workspaceId();
        }
        return null;
    }

    protected static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected RoleSessionScope requireRoleSessionScope() {
        ConsoleSessionScope sessionScope = ConsoleSessionContext.scope();
        ConsoleOwnership sessionOwnership = ConsoleOwnershipScope.requireRoleSessionOwnership();
        if (sessionScope == ConsoleSessionScope.PLATFORM) {
            return new RoleSessionScope(null, sessionOwnership);
        }
        String tenantId = Checks.notBlank(SessionContext.tenantId(), "tenantId");
        return new RoleSessionScope(tenantId, sessionOwnership);
    }

    /**
     * 角色 API 会话边界：由 {@link ConsoleSessionContext} 决定平台 / 租户 / 工作区层级。
     */
    protected record RoleSessionScope(String tenantId, ConsoleOwnership sessionOwnership) {

        String workspaceId() {
            if (sessionOwnership.ownerType() == RoleOwnerType.WORKSPACE) {
                return sessionOwnership.ownerId();
            }
            return null;
        }

        String securityRealm() {
            return sessionOwnership.securityRealm();
        }
    }
}
