package com.innospots.nexus.platform.auth.adapter;

import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;

/**
 * 平台域 {@link UserDirectory} 实现。
 */
@RequiredArgsConstructor
public class PlatformUserDirectory implements UserDirectory {

    private final PlatformUserDao platformUserDao;

    @Override
    public Optional<AuthUser> findByLogin(String identity) {
        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }
        PlatformUserEntity user = resolveUser(identity);
        if (user == null || !PlatformUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, identity));
    }

    @Override
    public Optional<AuthUser> findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        PlatformUserEntity user = platformUserDao.selectById(userId);
        if (user == null || !PlatformUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, user.getLoginName()));
    }

    private static AuthUser toAuthUser(PlatformUserEntity user, String loginName) {
        return new AuthUser(
                user.getPlatformUserId(),
                loginName,
                user.getStatus(),
                SecurityRealm.PLATFORM);
    }

    private PlatformUserEntity resolveUser(String identity) {
        PlatformUserEntity byLogin = platformUserDao.selectOne(new LambdaQueryWrapper<PlatformUserEntity>()
                .eq(PlatformUserEntity::getLoginName, identity));
        if (byLogin != null) {
            return byLogin;
        }
        PlatformUserEntity byEmail = platformUserDao.selectOne(new LambdaQueryWrapper<PlatformUserEntity>()
                .eq(PlatformUserEntity::getEmail, identity));
        if (byEmail != null) {
            return byEmail;
        }
        return platformUserDao.selectOne(new LambdaQueryWrapper<PlatformUserEntity>()
                .eq(PlatformUserEntity::getMobile, identity));
    }
}
