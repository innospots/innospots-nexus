package com.innospots.nexus.console.credential.password.service;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.ownership.CredentialOwnershipResolver;
import com.innospots.nexus.console.credential.password.PasswordValidator;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.password.policy.LoginLockPolicy;
import com.innospots.nexus.console.scope.ConsoleOwnership;

/**
 * 租户/平台用户密码生命周期：注册、修改、重置；校验强度后委托 {@link UserCredentialOperator}。
 * 供 portal / platform 调用，不绑定 HTTP。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.PasswordValidator
 * @see com.innospots.nexus.console.credential.password.operator.UserCredentialOperator
 */
@Slf4j
public final class CredentialService {

    private final UserCredentialOperator credentialOperator;
    private final PasswordValidator passwordValidator;
    private final LoginLockPolicy loginLockPolicy;

    /**
     * @param credentialOperator 凭据表操作
     * @param passwordValidator    本地密码强度策略
     * @param loginLockPolicy      登录失败锁定策略
     */
    public CredentialService(
            UserCredentialOperator credentialOperator,
            PasswordValidator passwordValidator,
            LoginLockPolicy loginLockPolicy
    ) {
        this.credentialOperator = Objects.requireNonNull(credentialOperator, "credentialOperator");
        this.passwordValidator = Objects.requireNonNull(passwordValidator, "passwordValidator");
        this.loginLockPolicy = Objects.requireNonNull(loginLockPolicy, "loginLockPolicy");
    }

    /**
     * 校验登录密码证明并更新失败次数与锁定状态；对外统一抛出 {@link NexusStatusCode#AUTHENTICATION_FAILED}。
     *
     * @param realm       安全域
     * @param subjectId   用户主体 ID
     * @param rawPassword 明文密码
     */
    public void authenticate(SecurityRealm realm, String subjectId, String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        CredentialRecord credential = credentialOperator.findPassword(realm, subjectId).orElse(null);
        if (credential == null) {
            log.debug("Login password proof rejected: no credential for subject in realm {}", realm);
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (credential.lockedUntil() != null && credential.lockedUntil().isAfter(LocalDateTime.now())) {
            log.debug("Login password proof rejected: subject locked until {}", credential.lockedUntil());
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (!credentialOperator.verify(credential, rawPassword)) {
            int failed = credential.failedAttempts() == null ? 1 : credential.failedAttempts() + 1;
            LocalDateTime lockedUntil = failed >= loginLockPolicy.maxFailedAttempts()
                    ? LocalDateTime.now().plusMinutes(loginLockPolicy.lockDurationMinutes())
                    : credential.lockedUntil();
            credentialOperator.saveState(realm, withAttempts(credential, failed, lockedUntil));
            log.debug("Login password proof rejected: failedAttempts={}", failed);
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        credentialOperator.saveState(realm, withAttempts(credential, 0, null));
    }

    /**
     * 首次为用户设置密码。
     * <p>调用场景：portal/platform 用户创建或邀请激活后写入初始密码。</p>
     *
     * @param realm       平台或租户域
     * @param subjectId   用户 ID
     * @param rawPassword 明文新密码
     */
    public void enrollPassword(SecurityRealm realm, String subjectId, String rawPassword) {
        validatePassword(rawPassword);
        credentialOperator.enrollPassword(CredentialOwnershipResolver.ownershipForRealm(realm), subjectId, rawPassword);
    }

    /**
     * 已登录用户修改密码（需校验旧密码）。
     * <p>调用场景：控制台或 portal 改密 API，在 OTP/会话已建立的前提下调用。</p>
     *
     * @param realm       安全域
     * @param subjectId   用户 ID
     * @param oldPassword 当前明文密码
     * @param newPassword 新明文密码
     * @throws NexusException 旧密码错误或新密码不符合策略时
     */
    public void changePassword(SecurityRealm realm, String subjectId, String oldPassword, String newPassword) {
        Objects.requireNonNull(oldPassword, "oldPassword");
        validatePassword(newPassword);
        if (oldPassword.equals(newPassword)) {
            throw NexusException.build(NexusStatusCode.BUSINESS_ERROR);
        }
        CredentialRecord current = credentialOperator.findPassword(realm, subjectId)
                .orElseThrow(() -> NexusException.build(NexusStatusCode.PASSWORD_ERROR));
        if (!credentialOperator.verify(current, oldPassword)) {
            throw NexusException.build(NexusStatusCode.PASSWORD_ERROR);
        }
        credentialOperator.enrollPassword(CredentialOwnershipResolver.ownershipForRealm(realm), subjectId, newPassword);
        credentialOperator.saveState(realm, withAttempts(current, 0, null));
    }

    /**
     * 在身份已验证（如 OTP）后强制设置新密码。
     * <p>调用场景：{@link com.innospots.nexus.portal.user.operator.PasswordOperator} 忘记密码流程，
     * 验证码通过后由 portal 调用。</p>
     *
     * @param realm       安全域
     * @param subjectId   用户 ID
     * @param newPassword 新明文密码
     */
    public void resetPassword(SecurityRealm realm, String subjectId, String newPassword) {
        validatePassword(newPassword);
        CredentialRecord current = credentialOperator.findPassword(realm, subjectId)
                .orElseThrow(() -> new IllegalStateException("No password credential for subject: " + subjectId));
        credentialOperator.enrollPassword(CredentialOwnershipResolver.ownershipForRealm(realm), subjectId, newPassword);
        CredentialRecord updated = credentialOperator.findPassword(realm, subjectId).orElse(current);
        credentialOperator.saveState(realm, new CredentialRecord(
                updated.subjectId(),
                updated.credentialKind(),
                updated.algorithm(),
                updated.verifier(),
                updated.verifierParams(),
                updated.credentialVersion(),
                0,
                null,
                true,
                updated.expiredAt()));
    }

    /**
     * 在显式归属下注册密码（租户资源级扩展场景）。
     * <p>调用场景：未来按租户 ID 隔离的凭据扩展；与 {@link CredentialOwnershipResolver#tenantResourceOwnership} 配合。</p>
     *
     * @param ownership   凭据归属
     * @param subjectId   用户 ID
     * @param rawPassword 明文密码
     */
    public void enrollPassword(ConsoleOwnership ownership, String subjectId, String rawPassword) {
        validatePassword(rawPassword);
        credentialOperator.enrollPassword(ownership, subjectId, rawPassword);
    }

    private void validatePassword(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        if (!passwordValidator.isValid(rawPassword)) {
            throw NexusException.build(NexusStatusCode.BUSINESS_ERROR);
        }
    }

    private static CredentialRecord withAttempts(CredentialRecord record, int failed, LocalDateTime lockedUntil) {
        return new CredentialRecord(
                record.subjectId(),
                record.credentialKind(),
                record.algorithm(),
                record.verifier(),
                record.verifierParams(),
                record.credentialVersion(),
                failed,
                lockedUntil,
                record.forceReset(),
                record.expiredAt());
    }
}
