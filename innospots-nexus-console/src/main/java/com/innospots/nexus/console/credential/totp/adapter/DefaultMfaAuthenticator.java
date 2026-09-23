package com.innospots.nexus.console.credential.totp.adapter;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.totp.MfaAuthenticator;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.credential.totp.TotpSecretProtector;
import com.innospots.nexus.console.credential.totp.service.TotpVerificationSupport;

/**
 * 从 {@code nx_user_credential} 加载已激活 TOTP 行并校验动态码。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.MfaAuthenticator
 */
public final class DefaultMfaAuthenticator implements MfaAuthenticator {

    private final UserCredentialOperator credentialOperator;
    private final TotpVerificationSupport verificationSupport;

    /**
     * @param credentialOperator 加载 TOTP 凭据行
     * @param secretProtector    解密 verifier 中的共享密钥
     */
    public DefaultMfaAuthenticator(
            UserCredentialOperator credentialOperator,
            TotpSecretProtector secretProtector
    ) {
        this.credentialOperator = Checks.notNull(credentialOperator, "credentialOperator");
        this.verificationSupport = new TotpVerificationSupport(secretProtector);
    }

    /**
     * 校验 TOTP；任何业务或校验失败均返回 {@code false}（不向外泄露具体原因）。
     */
    @Override
    public boolean verifyTotp(SecurityRealm realm, String subjectId, String code) {
        try {
            CredentialRecord record = credentialOperator.findCredential(realm, CredentialKind.TOTP, subjectId)
                    .orElse(null);
            TotpVerificationSupport.requireActiveTotp(record);
            verificationSupport.verifyCode(record, code);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
