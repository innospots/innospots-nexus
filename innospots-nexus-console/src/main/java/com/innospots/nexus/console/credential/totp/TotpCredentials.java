package com.innospots.nexus.console.credential.totp;

import com.innospots.nexus.console.auth.domain.model.CredentialRecord;

/**
 * TOTP 凭据行 {@code verifierParams} 约定与判定辅助。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService
 */
public final class TotpCredentials {

    /**
     * 写入 {@code verifierParams} 表示注册尚未通过首码确认。
     */
    public static final String ENROLLMENT_PENDING = "enrollment_pending";

    private TotpCredentials() {
    }

    /**
     * 判断 TOTP 行是否仍处于待确认注册状态。
     * <p>调用场景：区分「可登录 MFA」与「仅完成扫码未确认」。</p>
     *
     * @param record 凭据记录，可为 null
     * @return {@code verifierParams} 等于 {@link #ENROLLMENT_PENDING} 时为 {@code true}
     */
    public static boolean isEnrollmentPending(CredentialRecord record) {
        return record != null && ENROLLMENT_PENDING.equals(record.verifierParams());
    }
}
