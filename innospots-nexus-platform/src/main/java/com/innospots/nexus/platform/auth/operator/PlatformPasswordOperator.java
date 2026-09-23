package com.innospots.nexus.platform.auth.operator;

import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.VerificationType;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;

/**
 * 平台域用户密码变更与重置。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformPasswordOperator {

    private final PlatformUserDao platformUserDao;
    private final CredentialService credentialService;
    private final PasswordVerificationOperator verificationOperator;

    @Transactional
    public void changePassword(String platformUserId, String oldPassword, String newPassword) {
        Objects.requireNonNull(platformUserId, "platformUserId must not be null");
        PlatformUserEntity user = platformUserDao.selectById(platformUserId);
        if (user == null) {
            throw NexusException.build(NexusStatusCode.USER_NOT_FOUND);
        }
        credentialService.changePassword(SecurityRealm.PLATFORM, platformUserId, oldPassword, newPassword);
        log.info("Platform password changed for user: {}", platformUserId);
    }

    @Transactional
    public void resetPassword(String identity, String verificationCode, VerificationType type, String newPassword) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(verificationCode, "verificationCode must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(newPassword, "newPassword must not be null");

        PlatformUserEntity user = resolveUser(identity);
        if (user == null) {
            throw NexusException.build(NexusStatusCode.USER_NOT_FOUND);
        }

        verifyCode(verificationOperator, identity, type, verificationCode);
        credentialService.resetPassword(SecurityRealm.PLATFORM, user.getPlatformUserId(), newPassword);
        log.info("Platform password reset for user: {} via {} code", user.getPlatformUserId(), type);
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

    private void verifyCode(PasswordVerificationOperator op, String identity, VerificationType type, String code) {
        Objects.requireNonNull(op, "PasswordVerificationOperator must not be null");
        if (!op.verifyVerificationCode(identity, type, code)) {
            throw NexusException.build(NexusStatusCode.BUSINESS_ERROR);
        }
        op.expireVerificationCode(identity, type);
    }
}
