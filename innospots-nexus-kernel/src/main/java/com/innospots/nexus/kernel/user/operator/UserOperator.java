package com.innospots.nexus.kernel.user.operator;

import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.data.DataPage;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.base.util.IdGenerator;
import com.innospots.nexus.kernel.user.enums.UserStatus;
import com.innospots.nexus.kernel.user.api.UserPasswordDecryptor;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserPasswordCredentialDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;
import com.innospots.nexus.kernel.user.domain.request.UserPageRequest;
import com.innospots.nexus.kernel.user.domain.request.UserPasswordRegisterRequest;
import com.innospots.nexus.kernel.user.domain.vo.UserProfileVO;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

/**
 * User data operator backed by MyBatis-Plus DAO objects.
 * <p>This class keeps persistence coordination small: complex registration
 * policies, password hashing, verification, and cross-module workflows belong
 * in service classes.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class UserOperator {

    private static final String DEFAULT_PASSWORD_ALGORITHM = "BCRYPT";
    private static final int DEFAULT_PASSWORD_VERSION = 1;
    private static final String USER_ID_PREFIX = "usr";
    private static final String PASSWORD_CREDENTIAL_ID_PREFIX = "upc";

    private final UserDao userDao;
    private final UserPasswordCredentialDao passwordCredentialDao;
    private final UserPasswordDecryptor passwordDecryptor;

    /**
     * Finds a user profile by identifier.
     *
     * @param userId user identifier
     * @return user profile when found
     */
    public Optional<UserProfileVO> findById(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.selectById(userId)).map(this::toProfile);
    }

    /**
     * Pages user profiles with optional fuzzy filters.
     *
     * @param request user page request
     * @return user profile page
     */
    public DataPage<UserProfileVO> pageUsers(UserPageRequest request) {
        UserPageRequest pageRequest = request == null ? new UserPageRequest() : request;
        IPage<UserEntity> selectedPage = userDao.selectPage(
                new Page<>(pageRequest.getPageNo(), pageRequest.getPageSize()),
                pageQuery(pageRequest)
        );
        return DataPage.of(
                selectedPage.getRecords().stream().map(this::toProfile).toList(),
                pageRequest.getPageNo(),
                pageRequest.getPageSize(),
                selectedPage.getTotal()
        );
    }

    /**
     * Deletes a user by identifier.
     *
     * @param userId user identifier
     * @return true when a row was deleted
     */
    @Transactional
    public boolean deleteUser(String userId) {
        if (userId == null) {
            return false;
        }
        return userDao.deleteById(userId) > 0;
    }

    /**
     * Freezes a user by disabling its lifecycle status.
     *
     * @param userId user identifier
     * @return true when a row was updated
     */
    @Transactional
    public boolean freezeUser(String userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setStatus(UserStatus.DISABLED.name());
        return userDao.updateById(user) > 0;
    }

    /**
     * Unfreezes a user by restoring its lifecycle status.
     *
     * @param userId user identifier
     * @return true when a row was updated
     */
    @Transactional
    public boolean unfreezeUser(String userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE.name());
        return userDao.updateById(user) > 0;
    }

    /**
     * Registers a user with local password credentials.
     *
     * @param request registration request with frontend encrypted password
     * @return created user profile
     */
    @Transactional
    public UserProfileVO registerWithPassword(UserPasswordRegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        UserEntity user = createUserEntity(request.userName(), request.displayName(), request.realName(),
                request.email(), request.mobile(), UserRegisterSource.PASSWORD);
        userDao.insert(user);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        String passwordSalt = CryptoUtils.generatePasswordSalt();
        UserPasswordCredentialEntity credential = new UserPasswordCredentialEntity();
        credential.setCredentialId(IdGenerator.monotonicUlid(PASSWORD_CREDENTIAL_ID_PREFIX));
        credential.setUserId(user.getUserId());
        credential.setPasswordHash(CryptoUtils.encryptPassword(rawPassword, passwordSalt));
        credential.setPasswordSalt(passwordSalt);
        credential.setPasswordAlgorithm(DEFAULT_PASSWORD_ALGORITHM);
        credential.setPasswordVersion(DEFAULT_PASSWORD_VERSION);
        credential.setForceReset(false);
        credential.setFailedAttempts(0);
        passwordCredentialDao.insert(credential);

        return toProfile(user);
    }

    private UserEntity createUserEntity(
            String userName,
            String displayName,
            String realName,
            String email,
            String mobile,
            UserRegisterSource registerSource
    ) {
        UserEntity user = new UserEntity();
        user.setUserId(IdGenerator.monotonicUlid(USER_ID_PREFIX));
        user.setUserName(userName);
        user.setDisplayName(displayName);
        user.setRealName(realName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRegisterSource(registerSource.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private QueryWrapper<UserEntity> pageQuery(UserPageRequest request) {
        QueryWrapper<UserEntity> query = new QueryWrapper<>();
        if (hasText(request.getInput())) {
            query.and(wrapper -> wrapper
                    .like("user_name", request.getInput())
                    .or()
                    .like("real_name", request.getInput())
                    .or()
                    .like("email", request.getInput())
                    .or()
                    .like("mobile", request.getInput()));
        }
        if (hasText(request.getUserName())) {
            query.like("user_name", request.getUserName());
        }
        if (hasText(request.getRealName())) {
            query.like("real_name", request.getRealName());
        }
        if (hasText(request.getEmail())) {
            query.like("email", request.getEmail());
        }
        if (hasText(request.getMobile())) {
            query.like("mobile", request.getMobile());
        }
        return query;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
