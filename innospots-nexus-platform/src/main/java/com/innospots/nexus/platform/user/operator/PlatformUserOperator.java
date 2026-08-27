package com.innospots.nexus.platform.user.operator;

import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.credential.api.PasswordDecryptor;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.dao.PlatformUserPasswordDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserPasswordEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

/**
 * Persists platform users and their local password credentials.
 * <p>There is no public self-registration path. Administrators create
 * accounts through this operator.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformUserOperator {

    private static final String DEFAULT_PASSWORD_ALGORITHM = "BCRYPT";
    private static final int DEFAULT_PASSWORD_VERSION = 1;

    private final PlatformUserDao platformUserDao;
    private final PlatformUserPasswordDao passwordDao;
    private final PasswordDecryptor passwordDecryptor;

    /**
     * Finds a platform user by identifier.
     *
     * @param platformUserId platform-realm user identifier
     * @return user summary when found
     */
    public Optional<PlatformUserVo> findById(String platformUserId) {
        if (platformUserId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(platformUserDao.selectById(platformUserId)).map(this::toVo);
    }

    /**
     * Creates a platform user with a local password. Does not issue tokens.
     *
     * @param request admin create request
     * @return created user summary
     */
    @Transactional
    public PlatformUserVo createWithPassword(PlatformUserCreateRequest request) {
        requireText(request == null ? null : request.loginName(), "loginName");
        requireText(request == null ? null : request.encryptedPassword(), "encryptedPassword");

        PlatformUserEntity user = new PlatformUserEntity();
        user.setLoginName(request.loginName());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setMobile(request.mobile());
        user.setEmployeeNo(request.employeeNo());
        user.setStatus(PlatformUserStatus.ACTIVE.name());
        platformUserDao.insert(user);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        String passwordSalt = CryptoUtils.generatePasswordSalt();
        PlatformUserPasswordEntity credential = new PlatformUserPasswordEntity();
        credential.setPlatformUserId(user.getPlatformUserId());
        credential.setPasswordHash(CryptoUtils.encryptPassword(rawPassword, passwordSalt));
        credential.setPasswordSalt(passwordSalt);
        credential.setPasswordAlgorithm(DEFAULT_PASSWORD_ALGORITHM);
        credential.setPasswordVersion(DEFAULT_PASSWORD_VERSION);
        credential.setForceReset(false);
        credential.setFailedAttempts(0);
        passwordDao.insert(credential);

        log.info("Created platform user {}", user.getPlatformUserId());
        return toVo(user);
    }

    private PlatformUserVo toVo(PlatformUserEntity entity) {
        return new PlatformUserVo(
                entity.getPlatformUserId(),
                entity.getLoginName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getEmployeeNo(),
                entity.getStatus()
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }
}
