package com.innospots.nexus.kernel.auth.adapter;

import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * 租户域 {@link UserDirectory} 实现。
 */
@RequiredArgsConstructor
public class KernelUserDirectory implements UserDirectory {

    private final UserDao userDao;

    @Override
    public Optional<AuthUser> findByLogin(String identity) {
        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }
        UserEntity user = resolveUser(identity);
        if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, identity));
    }

    @Override
    public Optional<AuthUser> findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        UserEntity user = userDao.selectById(userId);
        if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, user.getUserName()));
    }

    private static AuthUser toAuthUser(UserEntity user, String loginName) {
        return new AuthUser(
                user.getTenantUserId(),
                loginName,
                user.getStatus(),
                SecurityRealm.TENANT);
    }

    private UserEntity resolveUser(String identity) {
        UserEntity byUserName = userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserName, identity));
        if (byUserName != null) {
            return byUserName;
        }
        UserEntity byEmail = userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, identity));
        if (byEmail != null) {
            return byEmail;
        }
        return userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getMobile, identity));
    }
}
