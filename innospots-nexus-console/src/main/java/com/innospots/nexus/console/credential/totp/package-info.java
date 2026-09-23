/**
 * TOTP（RFC 6238）：共享密钥加密落库、注册确认与 {@link com.innospots.nexus.console.credential.totp.MfaAuthenticator} 实现。
 * 登录 MFA 步进由 {@code auth} 编排，本包仅提供凭据能力。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.service.TotpEnrollmentService
 * @see com.innospots.nexus.console.credential.password.operator.UserCredentialOperator
 */
package com.innospots.nexus.console.credential.totp;
