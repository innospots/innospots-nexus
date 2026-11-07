package com.innospots.nexus.console.user.portal.operator;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.console.user.portal.dao.PortalUserDao;
import com.innospots.nexus.console.user.portal.dao.PortalUserOauthIdentityDao;
import com.innospots.nexus.console.user.portal.domain.entity.PortalUserEntity;
import com.innospots.nexus.console.user.portal.domain.entity.PortalUserOauthIdentityEntity;
import com.innospots.nexus.console.user.portal.domain.enums.PortalUserRegisterSource;
import com.innospots.nexus.console.user.portal.domain.enums.PortalUserStatus;
import com.innospots.nexus.console.user.portal.domain.request.PortalUserOauthRegisterRequest;
import com.innospots.nexus.console.user.portal.domain.vo.PortalUserProfileVo;

/**
 * 基于 MyBatis-Plus DAO 的 OAuth 租户用户数据操作器。
 * <p>注册仅创建登录身份，不创建租户成员关系。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class PortalUserOauthOperator {

    private final PortalUserDao userDao;
    private final PortalUserOauthIdentityDao oauthIdentityDao;

    /**
     * 使用 OAuth 身份绑定注册租户域身份。
     *
     * @param request OAuth registration 请求
     * @return created 用户档案
     */
    @Transactional
    public PortalUserProfileVo registerWithOauth(PortalUserOauthRegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        PortalUserEntity user = createUserEntity(request);
        userDao.insert(user);

        PortalUserOauthIdentityEntity identity = new PortalUserOauthIdentityEntity();
        identity.setPortalUserId(user.getPortalUserId());
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

    private PortalUserEntity createUserEntity(PortalUserOauthRegisterRequest request) {
        PortalUserEntity user = new PortalUserEntity();
        user.setUserName(request.userName());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setMobile(request.mobile());
        user.setRegion(request.region());
        user.setTimeZone(request.timeZone());
        user.setLanguage(request.language());
        user.setRegisterSource(PortalUserRegisterSource.OAUTH.name());
        user.setStatus(PortalUserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private PortalUserProfileVo toProfile(PortalUserEntity entity) {
        return new PortalUserProfileVo(
                entity.getPortalUserId(),
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

    private PortalUserRegisterSource parseRegisterSource(String value) {
        return value == null ? null : PortalUserRegisterSource.valueOf(value);
    }

    private PortalUserStatus parseStatus(String value) {
        return value == null ? null : PortalUserStatus.valueOf(value);
    }
}
