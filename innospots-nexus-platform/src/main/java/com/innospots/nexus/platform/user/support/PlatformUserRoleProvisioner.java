package com.innospots.nexus.platform.user.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;

/**
 * 在无会话上下文的开通流程中，将平台用户绑定到 PLATFORM 作用域角色。
 */
@RequiredArgsConstructor
public class PlatformUserRoleProvisioner {

    private static final ConsoleOwnership PLATFORM_ROLE_OWNERSHIP = new ConsoleOwnership(
            RoleOwnerType.PLATFORM,
            null,
            SecurityRealm.PLATFORM.name());

    private final RoleDao roleDao;
    private final RoleBindingDao roleBindingDao;

    /**
     * 按逗号分隔的角色编码授予绑定；空或空白字符串时无操作。
     *
     * @param platformUserId    平台用户 ID
     * @param defaultRoleCodesCsv 邀请或审批单上的默认角色编码
     */
    @Transactional
    public void assignDefaultRolesIfPresent(String platformUserId, String defaultRoleCodesCsv) {
        if (defaultRoleCodesCsv == null || defaultRoleCodesCsv.isBlank()) {
            return;
        }
        Checks.notBlank(platformUserId, "platformUserId");
        for (String roleCode : parseRoleCodes(defaultRoleCodesCsv)) {
            RoleEntity role = requireEnabledPlatformRole(roleCode);
            bindUserToRoleIfAbsent(platformUserId, role.getRoleId());
        }
    }

    private RoleEntity requireEnabledPlatformRole(String roleCode) {
        RoleEntity role = roleDao.selectOne(ConsoleOwnershipScope.applyRole(
                new LambdaQueryWrapper<RoleEntity>()
                        .eq(RoleEntity::getRoleCode, roleCode)
                        .eq(RoleEntity::getStatus, BasicStatus.ENABLED.name()),
                PLATFORM_ROLE_OWNERSHIP));
        if (role == null) {
            throw NexusException.build(PlatformUserStatusCode.ROLE_CODE_NOT_FOUND);
        }
        return role;
    }

    private void bindUserToRoleIfAbsent(String platformUserId, String roleId) {
        long existing = roleBindingDao.selectCount(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<RoleBindingEntity>()
                        .eq(RoleBindingEntity::getRoleId, roleId)
                        .eq(RoleBindingEntity::getSubjectType, RoleBindingSubjectType.USER.name())
                        .eq(RoleBindingEntity::getSubjectId, platformUserId),
                PLATFORM_ROLE_OWNERSHIP));
        if (existing > 0) {
            return;
        }
        RoleBindingEntity binding = new RoleBindingEntity();
        ConsoleOwnershipScope.stamp(binding, PLATFORM_ROLE_OWNERSHIP);
        binding.setRoleId(roleId);
        binding.setSubjectType(RoleBindingSubjectType.USER.name());
        binding.setSubjectId(platformUserId);
        roleBindingDao.insert(binding);
    }

    private static Set<String> parseRoleCodes(String defaultRoleCodesCsv) {
        Set<String> codes = new LinkedHashSet<>();
        Arrays.stream(defaultRoleCodesCsv.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .forEach(codes::add);
        return codes;
    }
}
