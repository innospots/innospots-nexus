package com.innospots.nexus.console.bootstrap.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;

/**
 * 启动引导第 4 阶段：按配置创建初始后台用户。
 *
 * <p><b>只负责用户本身</b>，角色绑定由第 5 阶段
 * {@link InitialUserRoleBindingService} 处理。拆分的理由：绑定依赖第 2 阶段已创建的
 * 角色，而用户创建不依赖任何前序阶段，分开后即使角色配置有误，也能先完成用户建档。</p>
 *
 * <p><b>幂等语义：</b>后台用户只能下线、不能删除，因此以 {@code loginName} 是否已存在
 * 作为唯一判据——已同步过的用户<b>完全跳过</b>，不重置密码、不改角色，
 * 避免覆盖运维在管理端的后续调整。</p>
 *
 * <p><b>初始密码：</b>当前配置为明文，仅在首次创建时写入凭证哈希；
 * 后续应改为外部密钥或加密载荷，并要求首次登录改密。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public class InitialUserSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialUserSyncService.class);

    private final AdminUserDao adminUserDao;
    private final CredentialService credentialService;

    /**
     * 创建初始用户同步服务。
     *
     * @param adminUserDao      后台用户 DAO
     * @param credentialService 凭证服务，用于写入初始密码
     */
    public InitialUserSyncService(AdminUserDao adminUserDao, CredentialService credentialService) {
        if (adminUserDao == null || credentialService == null) {
            throw new IllegalArgumentException("Initial user sync dependencies are required");
        }
        this.adminUserDao = adminUserDao;
        this.credentialService = credentialService;
    }

    /**
     * 用户同步结果。
     *
     * @param created 新建用户数
     * @param skipped 已存在而跳过的用户数
     */
    public record SyncResult(int created, int skipped) {
    }

    /**
     * 执行初始用户创建。
     *
     * @param users 配置声明的初始用户
     * @return 同步结果
     */
    @Transactional
    public SyncResult sync(List<InitialUser> users) {
        if (users == null || users.isEmpty()) {
            return new SyncResult(0, 0);
        }
        int created = 0;
        int skipped = 0;
        for (InitialUser user : users) {
            String loginName = user.loginName().trim();
            if (findByLoginName(loginName).isPresent()) {
                // 用户只能下线不能删除；已同步过则不再重复创建，也不覆盖密码。
                skipped++;
                continue;
            }
            SecurityRealm realm = parseRealm(user.securityRealm());
            AdminUserEntity entity = createUser(user, loginName, realm);
            credentialService.enrollPassword(realm, entity.getAdminUserId(), user.password());
            created++;
            LOGGER.info(
                    "Initial user created loginName={} realm={} adminUserId={}",
                    loginName,
                    realm,
                    entity.getAdminUserId());
        }
        LOGGER.info("Initial user sync finished created={} skipped={}", created, skipped);
        return new SyncResult(created, skipped);
    }

    /**
     * 按登录名查找已存在的后台用户。
     *
     * @param loginName 登录名
     * @return 用户实体
     */
    public Optional<AdminUserEntity> findByLoginName(String loginName) {
        if (loginName == null || loginName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(adminUserDao.selectOne(Wrappers.<AdminUserEntity>lambdaQuery()
                .eq(AdminUserEntity::getLoginName, loginName.trim())
                .last("LIMIT 1")));
    }

    /**
     * 解析配置中的安全域，缺省为 {@code PLATFORM}。
     *
     * @param value 配置值
     * @return 安全域枚举
     */
    public static SecurityRealm parseRealm(String value) {
        if (value == null || value.isBlank()) {
            return SecurityRealm.PLATFORM;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (SecurityRealm candidate : SecurityRealm.values()) {
            if (candidate.name().equals(normalized)) {
                return candidate;
            }
        }
        throw ConsoleExceptions.build(
                NexusStatusCode.CONFIG_ERROR.fullCode(),
                "Invalid securityRealm for initial user: " + value);
    }

    private AdminUserEntity createUser(InitialUser user, String loginName, SecurityRealm realm) {
        if (user.password() == null || user.password().isBlank()) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Initial user password is required: " + loginName);
        }
        AdminUserEntity entity = new AdminUserEntity();
        entity.setLoginName(loginName);
        entity.setDisplayName(user.displayName() == null || user.displayName().isBlank()
                ? loginName
                : user.displayName().trim());
        entity.setEmail(user.email());
        entity.setMobile(user.mobile());
        entity.setSecurityRealm(realm.name());
        entity.setStatus(AdminUserStatus.ACTIVE.name());
        adminUserDao.insert(entity);
        return entity;
    }
}
