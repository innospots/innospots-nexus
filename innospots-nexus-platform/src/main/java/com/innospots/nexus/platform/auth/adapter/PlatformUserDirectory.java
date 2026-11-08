package com.innospots.nexus.platform.auth.adapter;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.platform.auth.support.PlatformUserIdentityResolver;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;

/**
 * 平台域 {@link UserDirectory} 实现。
 */
@RequiredArgsConstructor
public class PlatformUserDirectory implements UserDirectory {

    private final AdminUserDao platformUserDao;
    private final PlatformUserIdentityResolver identityResolver;

    @Override
    public Optional<AuthUser> findByLogin(String identity) {
        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }
        AdminUserEntity user = identityResolver.findByIdentity(identity);
        if (user == null || !AdminUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, identity));
    }

    @Override
    public Optional<AuthUser> findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        AdminUserEntity user = platformUserDao.selectById(userId);
        if (user == null || !AdminUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, user.getLoginName()));
    }

    private static AuthUser toAuthUser(AdminUserEntity user, String loginName) {
        return new AuthUser(
                user.getAdminUserId(),
                loginName,
                user.getStatus(),
                SecurityRealm.PLATFORM);
    }
}
