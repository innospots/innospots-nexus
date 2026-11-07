package com.innospots.nexus.portal.auth.adapter;

import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.user.portal.dao.PortalUserDao;
import com.innospots.nexus.console.user.portal.domain.entity.PortalUserEntity;
import com.innospots.nexus.console.user.portal.domain.enums.PortalUserStatus;

/**
 * 租户域 {@link UserDirectory} 实现。
 */
@RequiredArgsConstructor
public class PortalUserDirectory implements UserDirectory {

    private final PortalUserDao userDao;

    @Override
    public Optional<AuthUser> findByLogin(String identity) {
        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }
        PortalUserEntity user = resolveUser(identity);
        if (user == null || !PortalUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, identity));
    }

    @Override
    public Optional<AuthUser> findById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        PortalUserEntity user = userDao.selectById(userId);
        if (user == null || !PortalUserStatus.ACTIVE.name().equals(user.getStatus())) {
            return Optional.empty();
        }
        return Optional.of(toAuthUser(user, user.getUserName()));
    }

    private static AuthUser toAuthUser(PortalUserEntity user, String loginName) {
        return new AuthUser(
                user.getPortalUserId(),
                loginName,
                user.getStatus(),
                SecurityRealm.TENANT);
    }

    private PortalUserEntity resolveUser(String identity) {
        PortalUserEntity byUserName = userDao.selectOne(new LambdaQueryWrapper<PortalUserEntity>()
                .eq(PortalUserEntity::getUserName, identity));
        if (byUserName != null) {
            return byUserName;
        }
        PortalUserEntity byEmail = userDao.selectOne(new LambdaQueryWrapper<PortalUserEntity>()
                .eq(PortalUserEntity::getEmail, identity));
        if (byEmail != null) {
            return byEmail;
        }
        return userDao.selectOne(new LambdaQueryWrapper<PortalUserEntity>()
                .eq(PortalUserEntity::getMobile, identity));
    }
}
