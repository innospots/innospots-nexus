package com.innospots.nexus.platform.auth.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.VerificationType;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.auth.status.PlatformAuthStatusCode;
import com.innospots.nexus.platform.auth.support.PlatformAuthSessionSupport;
import com.innospots.nexus.platform.auth.support.PlatformUserIdentityResolver;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;

/**
 * 运营管理平台密码修改与重置编排（凭据归属 console {@link CredentialService}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformPublicPasswordResetEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformPasswordService {

    private static final SecurityRealm REALM = SecurityRealm.PLATFORM;

    private final PlatformUserOperator platformUserOperator;
    private final PlatformUserIdentityResolver identityResolver;
    private final CredentialService credentialService;
    private final PasswordVerificationOperator verificationOperator;
    private final PasswordDecryptor passwordDecryptor;

    /**
     * 修改当前平台用户密码。
     *
     * @param request 含 RSA 加密后的旧/新密码
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        Checks.notNull(request, "request");
        String platformUserId = PlatformAuthSessionSupport.requirePlatformUserId();
        platformUserOperator.requireById(platformUserId);
        String oldPassword = passwordDecryptor.decrypt(request.oldEncryptedPassword());
        String newPassword = passwordDecryptor.decrypt(request.newEncryptedPassword());
        credentialService.changePassword(REALM, platformUserId, oldPassword, newPassword);
        log.info("Platform password changed for user: {}", platformUserId);
    }

    /**
     * 凭验证码重置密码（匿名）。
     *
     * @param request 身份、验证码与新密码
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        Checks.notNull(request, "request");
        String identity = request.identity();
        String verificationCode = request.verificationCode();
        VerificationType type = request.type();
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(verificationCode, "verificationCode must not be null");
        Objects.requireNonNull(type, "type must not be null");

        PlatformUserEntity user = identityResolver.findByIdentity(identity);
        if (user == null) {
            throw NexusException.build(PlatformUserStatusCode.PLATFORM_USER_NOT_FOUND);
        }

        verifyResetCode(identity, type, verificationCode);
        String newPassword = passwordDecryptor.decrypt(request.newEncryptedPassword());
        credentialService.resetPassword(REALM, user.getPlatformUserId(), newPassword);
        log.info("Platform password reset for user: {} via {} code", user.getPlatformUserId(), type);
    }

    private void verifyResetCode(String identity, VerificationType type, String code) {
        if (!verificationOperator.verifyVerificationCode(identity, type, code)) {
            throw NexusException.build(PlatformAuthStatusCode.PASSWORD_RESET_VERIFICATION_INVALID);
        }
        verificationOperator.expireVerificationCode(identity, type);
    }
}
