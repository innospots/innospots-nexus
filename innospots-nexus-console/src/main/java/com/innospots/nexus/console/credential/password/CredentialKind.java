package com.innospots.nexus.console.credential.password;

/**
 * {@code nx_user_credential.credential_kind} 取值；与 {@link com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithm} 解耦。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity
 */
public enum CredentialKind {

    /**
     * 本地密码（验证材料为单向哈希等 opaque verifier）。
     */
    PASSWORD,

    /**
     * RFC 6238 TOTP（共享密钥存于 verifier / verifierParams）；注册编排见 {@code totp} 子包。
     */
    TOTP
}
