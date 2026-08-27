package com.innospots.nexus.kernel.user.operator;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserOauthIdentityDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.kernel.user.domain.enums.UserRegisterSource;
import com.innospots.nexus.kernel.user.domain.enums.UserStatus;
import com.innospots.nexus.kernel.user.domain.request.UserOauthRegisterRequest;
import com.innospots.nexus.kernel.user.domain.vo.UserProfileVo;

/**
 * OAuth tenant-user data operator backed by MyBatis-Plus DAO objects.
 * <p>Registration creates a login identity only. It does not create a
 * tenant membership.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class UserOauthOperator {

    private final UserDao userDao;
    private final UserOauthIdentityDao oauthIdentityDao;

    /**
     * Registers a tenant-realm identity with an OAuth identity binding.
     *
     * @param request OAuth registration request
     * @return created user profile
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
