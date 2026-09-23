package com.innospots.nexus.console.credential.totp.service;

import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.ownership.CredentialOwnershipResolver;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.credential.totp.TotpCredentials;
import com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithms;
import com.innospots.nexus.console.credential.totp.TotpProvisioningUriBuilder;
import com.innospots.nexus.console.credential.totp.TotpSecretProtector;
import com.innospots.nexus.console.credential.totp.domain.TotpEnrollmentMaterial;
import com.innospots.nexus.console.credential.totp.status.TotpStatusCode;

/**
 * 默认 TOTP 注册：密钥经 {@link com.innospots.nexus.console.credential.totp.TotpSecretProtector} 加密后写入
 * {@link com.innospots.nexus.console.credential.password.CredentialKind#TOTP} 行，待确认状态见 {@link com.innospots.nexus.console.credential.totp.TotpCredentials}。
 *
 * @author Smars
 * @date 2026/09/19
 * @see TotpEnrollmentService
 */
public final class DefaultTotpEnrollmentService implements TotpEnrollmentService {

    private final UserCredentialOperator credentialOperator;
    private final TotpSecretProtector secretProtector;
    private final TotpVerificationSupport verificationSupport;

    /**
     * @param credentialOperator 读写 {@code CredentialKind#TOTP} 行
     * @param secretProtector    共享密钥生成与静态加密
     */
    public DefaultTotpEnrollmentService(
            UserCredentialOperator credentialOperator,
            TotpSecretProtector secretProtector
    ) {
        this.credentialOperator = Checks.notNull(credentialOperator, "credentialOperator");
        this.secretProtector = Checks.notNull(secretProtector, "secretProtector");
        this.verificationSupport = new TotpVerificationSupport(secretProtector);
    }

    @Override
    public TotpEnrollmentMaterial beginEnrollment(SecurityRealm realm, String subjectId, String accountLabel) {
        Checks.notNull(realm, "realm");
        Checks.notBlank(subjectId, "subjectId");
        Checks.notBlank(accountLabel, "accountLabel");
        Optional<CredentialRecord> existing = credentialOperator.findCredential(realm, CredentialKind.TOTP, subjectId);
        if (existing.isPresent() && !TotpCredentials.isEnrollmentPending(existing.get())) {
            throw NexusException.build(TotpStatusCode.ALREADY_ENROLLED);
        }
        // 待确认状态通过 verifierParams 标记，激活后由 confirmEnrollment 清除
        String base32Secret = secretProtector.generateBase32Secret();
        String encrypted = secretProtector.encryptForStorage(base32Secret);
        credentialOperator.saveVerifier(
                CredentialOwnershipResolver.ownershipForRealm(realm),
                subjectId,
                CredentialKind.TOTP,
                TotpCredentialAlgorithms.RFC6238_SHA1_V1,
                encrypted,
                TotpCredentials.ENROLLMENT_PENDING);
        String uri = TotpProvisioningUriBuilder.build(accountLabel, base32Secret);
        return new TotpEnrollmentMaterial(base32Secret, uri);
    }

    @Override
    public void confirmEnrollment(SecurityRealm realm, String subjectId, String code) {
        CredentialRecord record = credentialOperator.findCredential(realm, CredentialKind.TOTP, subjectId)
                .orElseThrow(() -> NexusException.build(TotpStatusCode.NOT_ENROLLED));
        TotpVerificationSupport.requirePendingTotp(record);
        verificationSupport.verifyCode(record, code);
        credentialOperator.clearVerifierParams(realm, CredentialKind.TOTP, subjectId);
    }

    @Override
    public void removeEnrollment(SecurityRealm realm, String subjectId) {
        Checks.notNull(realm, "realm");
        Checks.notBlank(subjectId, "subjectId");
        credentialOperator.deleteCredential(realm, CredentialKind.TOTP, subjectId);
    }
}
