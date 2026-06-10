package com.innospots.nexus.kernel.user.operator;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.util.IdGenerator;
import com.innospots.nexus.kernel.user.enums.UserStatus;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserOauthIdentityDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserOauthIdentityEntity;
import com.innospots.nexus.kernel.user.domain.request.UserOauthRegisterRequest;
import com.innospots.nexus.kernel.user.domain.vo.UserProfileVO;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

/**
 * OAuth user data operator backed by MyBatis-Plus DAO objects.
 */
@Slf4j
@RequiredArgsConstructor
public class UserOauthOperator {

    private static final String USER_ID_PREFIX = "usr";
    private static final String OAUTH_IDENTITY_ID_PREFIX = "uoi";

    private final UserDao userDao;
    private final UserOauthIdentityDao oauthIdentityDao;

    /**
     * Registers a user with an OAuth identity binding.
     *
     * @param request OAuth registration request
     * @return created user profile
     */
    @Transactional
    public UserProfileVO registerWithOauth(UserOauthRegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        UserEntity user = createUserEntity(request);
        userDao.insert(user);

        UserOauthIdentityEntity identity = new UserOauthIdentityEntity();
        identity.setIdentityId(IdGenerator.monotonicUlid(OAUTH_IDENTITY_ID_PREFIX));
        identity.setUserId(user.getUserId());
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
        user.setUserId(IdGenerator.monotonicUlid(USER_ID_PREFIX));
        user.setUserName(request.userName());
        user.setDisplayName(request.displayName());
        user.setRealName(request.realName());
        user.setEmail(request.email());
        user.setMobile(request.mobile());
        user.setRegisterSource(UserRegisterSource.OAUTH.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private UserProfileVO toProfile(UserEntity entity) {
        return new UserProfileVO(
                entity.getUserId(),
                entity.getUserName(),
                entity.getDisplayName(),
                entity.getRealName(),
                entity.getEmail(),
                entity.getMobile(),
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
