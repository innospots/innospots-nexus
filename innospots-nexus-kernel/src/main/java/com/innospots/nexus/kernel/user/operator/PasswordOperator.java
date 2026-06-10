package com.innospots.nexus.kernel.user.operator;

import java.util.NoSuchElementException;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.dao.UserPasswordCredentialDao;
import com.innospots.nexus.kernel.user.domain.entity.UserEntity;
import com.innospots.nexus.kernel.user.domain.entity.UserPasswordCredentialEntity;
import com.innospots.nexus.kernel.user.enums.VerificationType;

/**
 * User password operation operator.
 * <p>This class owns all password-related business logic:
 * changing a password when the old one is known, and resetting
 * a password via a verification code.  Password hashing and salt
 * use the same constants and algorithms as {@link com.innospots.nexus.kernel.user.operator.UserOperator}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordOperator {

    private final UserDao userDao;
    private final UserPasswordCredentialDao passwordCredentialDao;
    private final UserPasswordDecryptor passwordDecryptor;
    private final PasswordVerificationOperator verificationOperator;
    private final PasswordValidator validator;

    /**
     * Changes the user's password.
     * <p>The caller must supply the current password which is verified
     * against the stored hash before the new password is accepted.</p>
     *
     * @param userId      the user identifier
     * @param oldPassword the current raw password
     * @param newPassword the desired new raw password
     */
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(oldPassword, "oldPassword must not be null");
        Objects.requireNonNull(newPassword, "newPassword must not be null");

        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            throw new NoSuchElementException("User not found: " + userId);
        }

        UserPasswordCredentialEntity credential = passwordCredentialDao.getByUserId(userId);
        if (credential == null) {
            throw new IllegalStateException("No password credential for user: " + userId);
        }

        boolean matches = CryptoUtils.verifyPassword(oldPassword, credential.getPasswordSalt(), credential.getPasswordHash());
        if (!matches) {
            throw new PasswordMismatch("Old password is incorrect");
        }

        if (oldPassword.equals(newPassword)) {
            throw new PasswordSame("New password must differ from old password");
        }

        if (!validator.isValid(newPassword)) {
            throw new PasswordWeak("New password does not meet minimum strength requirements");
        }

        String newSalt = CryptoUtils.generatePasswordSalt();
        String newHash = CryptoUtils.encryptPassword(newPassword, newSalt);

        credential.setPasswordHash(newHash);
        credential.setPasswordSalt(newSalt);
        credential.setPasswordVersion(credential.getPasswordVersion() + 1);
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);

        passwordCredentialDao.updateById(credential);

        log.info("Password changed for user: {}", userId);
    }

    /**
     * Resets the user's password using a verification code.
     * <p>The identity can be userName, email, or mobile.  The verification
     * code proves the caller owns that identity.  After a successful
     * reset the user will be forced to change their password on next
     * login (forceReset = true).</p>
     *
     * @param identity           the userName, email, or mobile that received the verification code
     * @param verificationCode   the code sent to the user
     * @param type               transport type (EMAIL or MOBILE)
     * @param newPassword        the desired new raw password
     */
    @Transactional
    public void resetPassword(String identity, String verificationCode, VerificationType type, String newPassword) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(verificationCode, "verificationCode must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(newPassword, "newPassword must not be null");

        UserEntity user = resolveUser(identity);
        if (user == null) {
            throw new NoSuchElementException("User not found for identity: " + identity);
        }

        verifyCode(identity, type, verificationCode);

        if (!validator.isValid(newPassword)) {
            throw new PasswordWeak("New password does not meet minimum strength requirements");
        }

        UserPasswordCredentialEntity credential = passwordCredentialDao.getByUserId(user.getUserId());
        if (credential == null) {
            throw new IllegalStateException("No password credential for user: " + user.getUserId());
        }

        String newSalt = CryptoUtils.generatePasswordSalt();
        String newHash = CryptoUtils.encryptPassword(newPassword, newSalt);

        credential.setPasswordHash(newHash);
        credential.setPasswordSalt(newSalt);
        credential.setPasswordVersion(credential.getPasswordVersion() + 1);
        credential.setForceReset(true);
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);

        passwordCredentialDao.updateById(credential);

        log.info("Password reset for user: {} via {} code", user.getUserId(), type);
    }

    private UserEntity resolveUser(String identity) {
        UserEntity byUserName = userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserName, identity)
        );
        if (byUserName != null) {
            return byUserName;
        }
        UserEntity byEmail = userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, identity));
        if (byEmail != null) {
            return byEmail;
        }
        UserEntity byMobile = userDao.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getMobile, identity)
        );
        if (byMobile != null) {
            return byMobile;
        }
        return null;
    }

    private void verifyCode(PasswordVerificationOperator op, String identity, VerificationType type, String code) {
        Objects.requireNonNull(op, "PasswordVerificationOperator must not be null");
        if (!op.verifyVerificationCode(identity, type, code)) {
            throw new CodeInvalid("Verification code is invalid or expired");
        }
        op.expireVerificationCode(identity, type);
    }
}
