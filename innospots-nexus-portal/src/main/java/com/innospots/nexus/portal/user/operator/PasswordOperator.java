package com.innospots.nexus.portal.user.operator;

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
import com.innospots.nexus.portal.user.dao.UserDao;
import com.innospots.nexus.portal.user.domain.entity.UserEntity;

/**
 * 租户域用户密码变更与重置；凭据持久化归属 console {@link CredentialService}。
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordOperator {

    private final UserDao userDao;
    private final CredentialService credentialService;
    private final PasswordVerificationOperator verificationOperator;

    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        Objects.requireNonNull(userId, "userId must not be null");
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw NexusException.build(NexusStatusCode.USER_NOT_FOUND);
        }
        credentialService.changePassword(SecurityRealm.TENANT, userId, oldPassword, newPassword);
        log.info("Password changed for user: {}", userId);
    }

    @Transactional
    public void resetPassword(String identity, String verificationCode, VerificationType type, String newPassword) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(verificationCode, "verificationCode must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(newPassword, "newPassword must not be null");

        UserEntity user = resolveUser(identity);
        if (user == null) {
            throw NexusException.build(NexusStatusCode.USER_NOT_FOUND);
        }

        verifyCode(verificationOperator, identity, type, verificationCode);
        credentialService.resetPassword(SecurityRealm.TENANT, user.getTenantUserId(), newPassword);
        log.info("Password reset for user: {} via {} code", user.getTenantUserId(), type);
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

    private void verifyCode(PasswordVerificationOperator op, String identity, VerificationType type, String code) {
        Objects.requireNonNull(op, "PasswordVerificationOperator must not be null");
        if (!op.verifyVerificationCode(identity, type, code)) {
            throw NexusException.build(NexusStatusCode.BUSINESS_ERROR);
        }
        op.expireVerificationCode(identity, type);
    }
}
