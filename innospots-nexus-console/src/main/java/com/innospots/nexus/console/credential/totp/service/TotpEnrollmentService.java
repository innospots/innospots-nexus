package com.innospots.nexus.console.credential.totp.service;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.totp.domain.TotpEnrollmentMaterial;

/**
 * TOTP 注册两阶段：{@link #beginEnrollment} 返回扫码材料，{@link #confirmEnrollment} 校验首码后激活行。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService
 * @see com.innospots.nexus.console.credential.totp.domain.TotpEnrollmentMaterial
 */
public interface TotpEnrollmentService {

    /**
     * 生成共享密钥并写入待确认凭据行，返回扫码材料。
     * <p>调用场景：用户开启 MFA，管理端返回 secret/otpauth URI 供 authenticator 扫码。</p>
     *
     * @param realm        安全域
     * @param subjectId    用户主体 ID
     * @param accountLabel 展示在 authenticator 中的账号标签（如邮箱）
     * @return 明文 secret 与 provisioning URI（仅本次响应下发，不得再次从库中读出明文）
     */
    TotpEnrollmentMaterial beginEnrollment(SecurityRealm realm, String subjectId, String accountLabel);

    /**
     * 校验首个动态码并激活 TOTP 凭据。
     * <p>调用场景：用户扫码后提交第一枚 TOTP，清除 {@code verifierParams} 中的待确认标记。</p>
     *
     * @param realm     安全域
     * @param subjectId 用户主体 ID
     * @param code      authenticator 显示的动态码
     */
    void confirmEnrollment(SecurityRealm realm, String subjectId, String code);

    /**
     * 移除用户的 TOTP 凭据。
     * <p>调用场景：用户关闭 MFA 或管理员强制解绑。</p>
     *
     * @param realm     安全域
     * @param subjectId 用户主体 ID
     */
    void removeEnrollment(SecurityRealm realm, String subjectId);
}
