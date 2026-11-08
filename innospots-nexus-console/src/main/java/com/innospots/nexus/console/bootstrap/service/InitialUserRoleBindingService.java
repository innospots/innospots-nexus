package com.innospots.nexus.console.bootstrap.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;

/**
 * 启动引导第 5 阶段：把初始用户绑定到配置声明的角色。
 *
 * <p><b>为什么单独成阶段：</b>绑定同时依赖第 2 阶段的角色与第 4 阶段的用户。
 * 单独成阶段后可以按用户是否已存在分别处理：</p>
 * <ul>
 *   <li>用户是本轮新建的 → 补全配置声明的全部角色绑定；</li>
 *   <li>用户已存在（历史已同步）→ <b>不动</b>其角色，避免覆盖运维在管理端的调整。</li>
 * </ul>
 *
 * <p>绑定按 {@code 角色 + USER 主体 + 安全域} 判重，重复启动不会产生重复行。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public class InitialUserRoleBindingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitialUserRoleBindingService.class);

    private final InitialUserSyncService initialUserSyncService;
    private final RoleDao roleDao;
    private final RoleBindingDao roleBindingDao;

    /**
     * 创建用户角色绑定服务。
     *
     * @param initialUserSyncService 用户同步服务，复用其用户定位规则
     * @param roleDao                角色 DAO
     * @param roleBindingDao         角色绑定 DAO
     */
    public InitialUserRoleBindingService(
            InitialUserSyncService initialUserSyncService,
            RoleDao roleDao,
            RoleBindingDao roleBindingDao
    ) {
        if (initialUserSyncService == null || roleDao == null || roleBindingDao == null) {
            throw new IllegalArgumentException("Initial user role binding dependencies are required");
        }
        this.initialUserSyncService = initialUserSyncService;
        this.roleDao = roleDao;
        this.roleBindingDao = roleBindingDao;
    }

    /**
     * 绑定结果。
     *
     * @param bound   新建绑定数
     * @param skipped 已存在绑定数
     */
    public record SyncResult(int bound, int skipped) {
    }

    /**
     * 执行用户角色绑定。
     *
     * @param users 配置声明的初始用户
     * @return 绑定结果
     */
    @Transactional
    public SyncResult sync(List<InitialUser> users) {
        if (users == null || users.isEmpty()) {
            return new SyncResult(0, 0);
        }
        int bound = 0;
        int skipped = 0;
        for (InitialUser user : users) {
            List<String> roleCodes = user.roleCodes();
            if (roleCodes == null || roleCodes.isEmpty()) {
                continue;
            }
            String loginName = user.loginName().trim();
            AdminUserEntity entity = initialUserSyncService.findByLoginName(loginName).orElse(null);
            if (entity == null) {
                // 用户阶段应已创建；缺失说明前序阶段被跳过。
                LOGGER.warn("Admin user not found when binding roles, skipped loginName={}", loginName);
                continue;
            }
            SecurityRealm realm = InitialUserSyncService.parseRealm(user.securityRealm());
            for (String rawCode : roleCodes) {
                if (rawCode == null || rawCode.isBlank()) {
                    continue;
                }
                String roleCode = rawCode.trim();
                RoleEntity role = findRole(roleCode, realm);
                if (role == null) {
                    throw ConsoleExceptions.build(
                            NexusStatusCode.CONFIG_ERROR.fullCode(),
                            "Initial user role not found: roleCode=" + roleCode
                                    + " realm=" + realm.name()
                                    + " (user " + loginName + ")");
                }
                if (isBound(role, entity.getAdminUserId())) {
                    skipped++;
                    continue;
                }
                roleBindingDao.insert(newBinding(role, entity.getAdminUserId()));
                bound++;
            }
        }
        LOGGER.info("Initial user role binding finished bound={} skipped={}", bound, skipped);
        return new SyncResult(bound, skipped);
    }

    private RoleEntity findRole(String roleCode, SecurityRealm realm) {
        return roleDao.selectOne(Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getRoleCode, roleCode)
                .eq(RoleEntity::getSecurityRealm, realm.name())
                .last("LIMIT 1"));
    }

    private boolean isBound(RoleEntity role, String adminUserId) {
        return roleBindingDao.selectCount(Wrappers.<RoleBindingEntity>lambdaQuery()
                .eq(RoleBindingEntity::getRoleId, role.getRoleId())
                .eq(RoleBindingEntity::getSubjectType, RoleBindingSubjectType.USER.name())
                .eq(RoleBindingEntity::getSubjectId, adminUserId)) > 0;
    }

    private RoleBindingEntity newBinding(RoleEntity role, String adminUserId) {
        RoleBindingEntity binding = new RoleBindingEntity();
        binding.setRoleId(role.getRoleId());
        binding.setSubjectType(RoleBindingSubjectType.USER.name());
        binding.setSubjectId(adminUserId);
        binding.setOwnerType(role.getOwnerType());
        binding.setOwnerId(role.getOwnerId());
        binding.setSecurityRealm(role.getSecurityRealm());
        return binding;
    }
}
