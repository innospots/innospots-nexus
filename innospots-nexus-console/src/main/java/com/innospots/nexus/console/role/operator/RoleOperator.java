package com.innospots.nexus.console.role.operator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
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
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 工作空间作用域内角色持久化与查询，可供子类扩展过滤或校验规则。
 */
@RequiredArgsConstructor
public class RoleOperator {

    private final RoleDao roleDao;
    private final RoleBindingDao roleBindingDao;
    private final RoleConverter roleConverter;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        RoleSessionScope scope = requireRoleSessionScope();
        RolePageRequest pageRequest = request == null ? new RolePageRequest() : request;
        LambdaQueryWrapper<RoleEntity> query = pageQuery(pageRequest, scope);
        IPage<RoleEntity> selectedPage = roleDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<RoleVo> records = selectedPage.getRecords().stream()
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.workspaceOwnership())))
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public RoleVo getRole(String roleId) {
        RoleSessionScope scope = requireRoleSessionScope();
        return findReadableEntity(roleId, scope)
                .map(entity -> roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.workspaceOwnership())))
                .orElseThrow(() -> NexusException.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    @Transactional
    public RoleVo createRole(RoleCreateRequest request) {
        ConsoleOwnershipGuard.requireWorkspaceScope();
        Objects.requireNonNull(request, "request");
        validateOwner(request.ownerType(), request.ownerId());
        if (existsRoleCode(
                request.ownerType(),
                request.ownerId(),
                request.securityRealm().name(),
                request.roleCode())) {
            throw NexusException.build(RoleStatusCode.ROLE_CODE_DUPLICATED);
        }
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(request.roleName());
        entity.setRoleCode(request.roleCode());
        entity.setOwnerType(request.ownerType().name());
        entity.setOwnerId(normalizeOwnerId(request.ownerType(), request.ownerId()));
        entity.setSecurityRealm(request.securityRealm().name());
        entity.setDescription(request.description());
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(false);
        entity.setAdministrator(false);
        roleDao.insert(entity);
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
        return roleConverter.toVo(entity, countMembers(entity.getRoleId(), scope.workspaceOwnership()));
    }

    @Transactional
    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        RoleSessionScope scope = requireRoleSessionScope();
        Objects.requireNonNull(request, "request");
        if (request.status() == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        RoleEntity entity = requireWritableEntity(roleId, scope);
        entity.setStatus(roleConverter.formatStatus(request.status()));
        roleDao.updateById(entity);
    }

    @Transactional
    public void deleteRole(String roleId) {
        RoleSessionScope scope = requireRoleSessionScope();
        RoleEntity entity = requireWritableEntity(roleId, scope);
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw NexusException.build(RoleStatusCode.ROLE_PROTECTED);
        }
        roleBindingDao.delete(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>().eq(RoleBindingEntity::getRoleId, roleId),
                scope.workspaceOwnership()));
        roleDao.deleteById(roleId);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        RoleSessionScope scope = requireRoleSessionScope();
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>()
                        .orderByAsc(RoleEntity::getSortOrder)
                        .orderByAsc(RoleEntity::getRoleName),
                scope.tenantId(),
                scope.workspaceId(),
                scope.securityRealm());
        if (status != null) {
            query.eq(RoleEntity::getStatus, status.name());
        }
        return roleDao.selectList(query).stream()
                .map(roleConverter::toOption)
                .toList();
    }

    protected RoleEntity requireEntity(String roleId, ConsoleOwnership ownership) {
        RoleSessionScope scope = requireRoleSessionScope();
        if (!scope.workspaceOwnership().equals(ownership)) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        return requireReadableEntity(roleId, scope);
    }

    protected RoleEntity requireReadableEntity(String roleId, RoleSessionScope scope) {
        return findReadableEntity(roleId, scope)
                .orElseThrow(() -> NexusException.build(RoleStatusCode.ROLE_NOT_FOUND));
    }

    protected RoleEntity requireWritableEntity(String roleId, RoleSessionScope scope) {
        RoleEntity entity = requireReadableEntity(roleId, scope);
        ConsoleOwnershipScope.assertRoleWritable(entity, scope.tenantId(), scope.workspaceOwnership());
        return entity;
    }

    protected Optional<RoleEntity> findReadableEntity(String roleId, RoleSessionScope scope) {
        if (roleId == null || roleId.isBlank()) {
            return Optional.empty();
        }
        RoleEntity entity = roleDao.selectOne(ConsoleOwnershipScope.applyRoleListVisibility(
                new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getRoleId, roleId),
                scope.tenantId(),
                scope.workspaceId(),
                scope.securityRealm()));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertRoleReadable(entity, scope.tenantId(), scope.workspaceOwnership());
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
                scope.tenantId(),
                scope.workspaceId(),
                scope.securityRealm());
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
            throw NexusException.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        if (ownerType == RoleOwnerType.PLATFORM) {
            return;
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw NexusException.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
    }

    protected static String normalizeOwnerId(RoleOwnerType ownerType, String ownerId) {
        if (ownerType == RoleOwnerType.PLATFORM) {
            return null;
        }
        return ownerId;
    }

    protected static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected RoleSessionScope requireRoleSessionScope() {
        ConsoleOwnership workspaceOwnership = ConsoleOwnershipGuard.requireWorkspaceScope();
        String tenantId = Checks.notBlank(SessionContext.tenantId(), "tenantId");
        return new RoleSessionScope(tenantId, workspaceOwnership);
    }

    protected record RoleSessionScope(String tenantId, ConsoleOwnership workspaceOwnership) {

        String workspaceId() {
            return workspaceOwnership.ownerId();
        }

        String securityRealm() {
            return workspaceOwnership.securityRealm();
        }
    }
}
