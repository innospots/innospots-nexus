package com.innospots.nexus.portal.user.operator;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.portal.user.dao.UserDao;
import com.innospots.nexus.portal.user.dao.UserOauthIdentityDao;
import com.innospots.nexus.portal.user.domain.entity.UserEntity;
import com.innospots.nexus.portal.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.portal.user.domain.enums.UserRegisterSource;
import com.innospots.nexus.portal.user.domain.enums.UserStatus;
import com.innospots.nexus.portal.user.domain.request.UserOauthRegisterRequest;
import com.innospots.nexus.portal.user.domain.vo.UserProfileVo;

/**
 * 基于 MyBatis-Plus DAO 的 OAuth 租户用户数据操作器。
 * <p>注册仅创建登录身份，不创建租户成员关系。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class UserOauthOperator {

    private final UserDao userDao;
    private final UserOauthIdentityDao oauthIdentityDao;

    /**
     * 使用 OAuth 身份绑定注册租户域身份。
     *
     * @param request OAuth registration 请求
     * @return created 用户档案
     */
    @Transactional
    public UserProfileVo registerWithOauth(UserOauthRegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        UserEntity user = createUserEntity(request);
        userDao.insert(user);

        UserOauthIdentityEntity identity = new UserOauthIdentityEntity();
        identity.setTenantUserId(user.getTenantUserId());
        identity.setProvider(request.provider());
        identity.setProviderSubject(request.providerSubject());
        identity.setProviderAccount(request.providerAccount());
        identity.setProviderDisplayName(request.providerDisplayName());
        identity.setProviderEmail(request.providerEmail());
        identity.setProviderAvatarUrl(request.providerAvatarUrl());
        identity.setAccessTokenKey(request.accessTokenKey());
        identity.setRefreshTokenKey(request.refreshTokenKey());
        identity.setTokenExpiresAt(request.tokenExpiresAt());
        oauthIdentityDao.insert(identity);

        return toProfile(user);
    }

    private UserEntity createUserEntity(UserOauthRegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setUserName(request.userName());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setMobile(request.mobile());
        user.setRegion(request.region());
        user.setTimeZone(request.timeZone());
        user.setLanguage(request.language());
        user.setRegisterSource(UserRegisterSource.OAUTH.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private UserProfileVo toProfile(UserEntity entity) {
        return new UserProfileVo(
                entity.getTenantUserId(),
                entity.getUserName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getRegion(),
                entity.getTimeZone(),
                entity.getLanguage(),
                entity.getAvatarKey(),
                parseRegisterSource(entity.getRegisterSource()),
                parseStatus(entity.getStatus())
        );
    }

    private UserRegisterSource parseRegisterSource(String value) {
        return value == null ? null : UserRegisterSource.valueOf(value);
    }

    private UserStatus parseStatus(String value) {
        return value == null ? null : UserStatus.valueOf(value);
    }
}
