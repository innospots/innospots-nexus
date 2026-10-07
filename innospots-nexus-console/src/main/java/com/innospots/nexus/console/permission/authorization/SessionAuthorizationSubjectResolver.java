package com.innospots.nexus.console.permission.authorization;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 从 {@link SessionContext} 与角色绑定表解析当前鉴权主体。
 */
public final class SessionAuthorizationSubjectResolver implements AuthorizationSubjectResolver {

    private final RoleBindingDao roleBindingDao;
    private final RoleDao roleDao;

    public SessionAuthorizationSubjectResolver(RoleBindingDao roleBindingDao, RoleDao roleDao) {
        if (roleBindingDao == null || roleDao == null) {
            throw new IllegalArgumentException("roleBindingDao and roleDao are required");
        }
        this.roleBindingDao = roleBindingDao;
        this.roleDao = roleDao;
    }

    @Override
    public Optional<AuthorizationSubject> resolve() {
        Optional<UserSnapshot> user = SessionContext.user();
        if (user.isEmpty()) {
            return Optional.empty();
        }
        try {
            ConsoleOwnership workspaceOwnership = ConsoleOwnershipGuard.requireWorkspaceScope();
            String tenantId = SessionContext.tenantId();
            if (tenantId == null || tenantId.isBlank()) {
                return Optional.empty();
            }
            String userId = String.valueOf(user.get().userId());
            List<RoleBindingEntity> bindings = roleBindingDao.selectList(
                    ConsoleOwnershipScope.apply(
                            Wrappers.<RoleBindingEntity>lambdaQuery()
                                    .eq(RoleBindingEntity::getSubjectType, RoleBindingSubjectType.USER.name())
                                    .eq(RoleBindingEntity::getSubjectId, userId),
                            workspaceOwnership));
            Set<String> roleIds = bindings.stream()
                    .map(RoleBindingEntity::getRoleId)
                    .collect(Collectors.toCollection(HashSet::new));
            boolean administrator = resolveAdministrator(
                    roleIds,
                    tenantId,
                    workspaceOwnership.ownerId(),
                    workspaceOwnership.securityRealm());
            return Optional.of(new AuthorizationSubject(userId, roleIds, Set.of(), administrator));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private boolean resolveAdministrator(
            Set<String> roleIds,
            String tenantId,
            String workspaceId,
            String securityRealm
    ) {
        if (roleIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<RoleEntity> query = ConsoleOwnershipScope.applyRoleListVisibility(
                Wrappers.<RoleEntity>lambdaQuery()
                        .in(RoleEntity::getRoleId, roleIds)
                        .eq(RoleEntity::getAdministrator, true),
                tenantId,
                workspaceId,
                securityRealm);
        return roleDao.selectCount(query) > 0;
    }
}
