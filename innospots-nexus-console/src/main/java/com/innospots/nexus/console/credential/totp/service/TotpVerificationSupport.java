package com.innospots.nexus.console.credential.totp.service;

import java.time.Instant;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.totp.TotpCredentials;
import com.innospots.nexus.console.credential.totp.TotpSecretProtector;
import com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm;
import com.innospots.nexus.console.credential.totp.status.TotpStatusCode;

/**
 * TOTP 动态码校验（注册确认与登录 MFA 共用）；失败抛出 {@link com.innospots.nexus.console.credential.totp.status.TotpStatusCode}。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm
 * @see com.innospots.nexus.console.credential.totp.adapter.DefaultMfaAuthenticator
 */
public final class TotpVerificationSupport {

    private final TotpSecretProtector secretProtector;
    private final TotpCredentialAlgorithm totpAlgorithm;

    /**
     * @param secretProtector 解密库中 verifier 并得到 HMAC 密钥字节
     */
    public TotpVerificationSupport(TotpSecretProtector secretProtector) {
        this.secretProtector = Checks.notNull(secretProtector, "secretProtector");
        this.totpAlgorithm = new TotpCredentialAlgorithm();
    }

    /**
     * 校验动态码是否与当前时间窗匹配。
     * <p>调用场景：{@link DefaultTotpEnrollmentService#confirmEnrollment} 与内部 MFA 校验路径。</p>
     *
     * @param record 含加密共享密钥的 TOTP 凭据行
     * @param code   用户输入
     * @throws NexusException 码无效时 {@link TotpStatusCode#CODE_INVALID}
     */
    public void verifyCode(CredentialRecord record, String code) {
        Checks.notBlank(code, "code");
        String base32 = secretProtector.decryptFromStorage(record.verifier());
        byte[] secret = secretProtector.decodeBase32Secret(base32);
        if (!totpAlgorithm.verify(secret, code, Instant.now())) {
            throw NexusException.build(TotpStatusCode.CODE_INVALID);
        }
    }

    /**
     * 要求凭据已激活（非待确认）。
     * <p>调用场景：登录 MFA 前断言用户已完成 {@link TotpEnrollmentService#confirmEnrollment}。</p>
     *
     * @param record 凭据记录，可为 null
     * @return 非 null 且已激活的记录
     * @throws NexusException 未注册或仍为待确认
     */
    public static CredentialRecord requireActiveTotp(CredentialRecord record) {
        if (record == null) {
            throw NexusException.build(TotpStatusCode.NOT_ENROLLED);
        }
        if (TotpCredentials.isEnrollmentPending(record)) {
            throw NexusException.build(TotpStatusCode.ENROLLMENT_PENDING);
        }
        return record;
    }

    /**
     * 要求凭据处于注册待确认状态。
     * <p>调用场景：{@link DefaultTotpEnrollmentService#confirmEnrollment} 仅接受 pending 行。</p>
     *
     * @param record 凭据记录，可为 null
     * @return 非 null 且待确认的记录
     * @throws NexusException 未注册或已激活
     */
    public static CredentialRecord requirePendingTotp(CredentialRecord record) {
        if (record == null) {
            throw NexusException.build(TotpStatusCode.NOT_ENROLLED);
        }
        if (!TotpCredentials.isEnrollmentPending(record)) {
            throw NexusException.build(TotpStatusCode.ALREADY_ENROLLED);
        }
        return record;
    }
}
