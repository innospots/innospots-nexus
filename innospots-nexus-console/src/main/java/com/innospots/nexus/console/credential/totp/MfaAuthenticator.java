package com.innospots.nexus.console.credential.totp;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * MFA 动态码校验端口；默认实现见 {@link com.innospots.nexus.console.credential.totp.adapter.DefaultMfaAuthenticator}。
 *
 * @author Smars
 * @date 2026/09/19
 * @see TotpEnrollmentService
 */
public interface MfaAuthenticator {

    /**
     * 校验用户提交的 TOTP 动态码。
     *
     * @param realm     安全域
     * @param subjectId 用户主体 ID
     * @param code      用户输入的动态码
     * @return 校验通过时为 {@code true}
     */
    boolean verifyTotp(SecurityRealm realm, String subjectId, String code);
}
