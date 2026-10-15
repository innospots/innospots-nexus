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
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 角色持久化基础操作：创建、查询、更新、删除；会话归属与参数适配由上层 service 负责。
 */
@Slf4j
@RequiredArgsConstructor
public class RoleOperator {

    private static final int DEFAULT_ROLE_SORT_ORDER = 1;

    private final RoleDao roleDao;
    private final RoleBindingDao roleBindingDao;
    private final RoleConverter roleConverter;

    public PageResult<RoleVo> pageRoles(RolePageRequest request, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        return pageRoles(request, ownership, roleVisibilityTenantId(ownership), roleVisibilityWorkspaceId(ownership));
    }

    public PageResult<RoleVo> pageRoles(
            RolePageRequest request,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Objects.requireNonNull(ownership, "ownership");
        RolePageRequest pageRequest = request == null ? new RolePageRequest() : request;
        LambdaQueryWrapper<RoleEntity> query = pageQuery(pageRequest, ownership, tenantId, workspaceId);
        IPage<RoleEntity> selectedPage = roleDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<RoleVo> records = selectedPage.getRecords().stream()
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), ownership)))
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public RoleVo getRole(String roleId, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        return getRole(roleId, ownership, roleVisibilityTenantId(ownership), roleVisibilityWorkspaceId(ownership));
    }

    public RoleVo getRole(String roleId, ConsoleOwnership ownership, String tenantId, String workspaceId) {
        Objects.requireNonNull(ownership, "ownership");
        return findReadableEntity(roleId, ownership, tenantId, workspaceId)
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), ownership)))
                .orElseThrow(() -> ConsoleExceptions.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    /**
     * 按已解析的归属创建角色（ownerType / ownerId / securityRealm 由调用方固化）。
     */
    @Transactional
    public RoleVo createRole(
            String roleName,
            String roleCode,
            String description,
            RoleOwnerType ownerType,
            String ownerId
    ) {
        if (ownerType == null) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        validateOwner(ownerType, ownerId);
        String securityRealm = ownerType.securityRealm().name();
        String normalizedOwnerId = normalizeOwnerId(ownerType, ownerId);
        if (existsRoleCode(ownerType, normalizedOwnerId, securityRealm, roleCode)) {
            throw ConsoleExceptions.build(RoleStatusCode.ROLE_CODE_DUPLICATED);
        }
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(roleName);
        entity.setRoleCode(roleCode);
        entity.setOwnerType(ownerType.name());
        entity.setOwnerId(normalizedOwnerId);
        entity.setSecurityRealm(securityRealm);
        entity.setDescription(description);
        entity.setSortOrder(DEFAULT_ROLE_SORT_ORDER);
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(false);
        entity.setAdministrator(false);
        roleDao.insert(entity);
        log.info("Created role {} with code {}", entity.getRoleId(), entity.getRoleCode());
        return roleConverter.toVo(entity, 0L);
    }

    @Transactional
    public RoleVo updateRole(String roleId, RoleUpdateRequest request, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        return updateRole(
                roleId,
                request,
                ownership,
                roleVisibilityTenantId(ownership),
                roleVisibilityWorkspaceId(ownership));
    }

    @Transactional
    public RoleVo updateRole(
            String roleId,
            RoleUpdateRequest request,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(ownership, "ownership");
        RoleEntity entity = requireWritableEntity(roleId, ownership, tenantId, workspaceId);
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
        return roleConverter.toVo(entity, countMembers(entity.getRoleId(), ownership));
    }

    @Transactional
    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        updateRoleStatus(
                roleId,
                request,
                ownership,
                roleVisibilityTenantId(ownership),
                roleVisibilityWorkspaceId(ownership));
    }

    @Transactional
    public void updateRoleStatus(
            String roleId,
            RoleStatusUpdateRequest request,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(ownership, "ownership");
        if (request.status() == null) {
            throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
        }
        RoleEntity entity = requireWritableEntity(roleId, ownership, tenantId, workspaceId);
        entity.setStatus(roleConverter.formatStatus(request.status()));
        roleDao.updateById(entity);
        log.info("Updated role {} status to {}", entity.getRoleId(), request.status());
    }

    @Transactional
    public void deleteRole(String roleId, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        deleteRole(roleId, ownership, roleVisibilityTenantId(ownership), roleVisibilityWorkspaceId(ownership));
    }

    @Transactional
    public void deleteRole(String roleId, ConsoleOwnership ownership, String tenantId, String workspaceId) {
        Objects.requireNonNull(ownership, "ownership");
        RoleEntity entity = requireWritableEntity(roleId, ownership, tenantId, workspaceId);
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw ConsoleExceptions.build(RoleStatusCode.ROLE_PROTECTED);
        }
        roleBindingDao.delete(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>().eq(RoleBindingEntity::getRoleId, roleId),
                ownership));
        roleDao.deleteById(roleId);
        log.info("Deleted role {}", roleId);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        return listRoleOptions(
                status,
                ownership,
                roleVisibilityTenantId(ownership),
                roleVisibilityWorkspaceId(ownership));
    }

    public List<RoleOptionVo> listRoleOptions(
            BasicStatus status,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Objects.requireNonNull(ownership, "ownership");
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSortOrder)
                        .orderByAsc(RoleEntity::getRoleName),
                ownership,
                tenantId,
                workspaceId);
        if (status != null) {
            query.eq(RoleEntity::getStatus, status.name());
        }
        return roleDao.selectList(query).stream()
                .map(roleConverter::toOption)
                .toList();
    }

    /**
     * 绑定等场景：在给定归属下读取角色实体。
     */
    public RoleEntity requireEntity(String roleId, ConsoleOwnership ownership) {
        Objects.requireNonNull(ownership, "ownership");
        String tenantId = resolveTenantId(ownership);
        String workspaceId = ownership.ownerType() == RoleOwnerType.WORKSPACE ? ownership.ownerId() : null;
        return findReadableEntity(roleId, ownership, tenantId, workspaceId)
                .orElseThrow(() -> ConsoleExceptions.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    protected RoleEntity requireReadableEntity(
            String roleId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        return findReadableEntity(roleId, ownership, tenantId, workspaceId)
                .orElseThrow(() -> ConsoleExceptions.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    protected RoleEntity requireWritableEntity(
            String roleId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        RoleEntity entity = requireReadableEntity(roleId, ownership, tenantId, workspaceId);
        ConsoleOwnershipScope.assertRoleWritable(entity, tenantId, ownership);
        return entity;
    }

    protected Optional<RoleEntity> findReadableEntity(
            String roleId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        if (roleId == null || roleId.isBlank()) {
            return Optional.empty();
        }
        RoleEntity entity = roleDao.selectOne(ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getRoleId, roleId),
                ownership,
                tenantId,
                workspaceId));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertRoleReadable(entity, tenantId, ownership);
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
        LambdaQueryWrapper<RoleEntity> query = new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getOwnerType, ownerType.name())
                .eq(RoleEntity::getSecurityRealm, securityRealm)
                .eq(RoleEntity::getRoleCode, roleCode);
        if (ownerId == null) {
            query.isNull(RoleEntity::getOwnerId);
        } else {
            query.eq(RoleEntity::getOwnerId, ownerId);
        }
        return roleDao.selectCount(query) > 0;
    }

    protected LambdaQueryWrapper<RoleEntity> pageQuery(
            RolePageRequest request,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSortOrder)
                        .orderByAsc(RoleEntity::getRoleName),
                ownership,
                tenantId,
                workspaceId);
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
            if (hasText(ownerId)) {
                throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
            }
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

    private static String resolveTenantId(ConsoleOwnership ownership) {
        return roleVisibilityTenantId(ownership);
    }

    private static String roleVisibilityTenantId(ConsoleOwnership ownership) {
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            return null;
        }
        if (ownership.ownerType() == RoleOwnerType.TENANT) {
            return ownership.ownerId();
        }
        return SessionContext.tenantId();
    }

    private static String roleVisibilityWorkspaceId(ConsoleOwnership ownership) {
        if (ownership.ownerType() == RoleOwnerType.WORKSPACE) {
            return ownership.ownerId();
        }
        return null;
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
