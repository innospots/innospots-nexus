package com.innospots.nexus.console.credential.password.algorithm;

/**
 * 内置凭据算法标识符。
 */
public final class CredentialAlgorithms {

    /**
     * BCrypt 单向验证；{@code verifier} 为完整 BCrypt 字符串。
     */
    public static final String BCRYPT_V1 = "bcrypt@v1";

    private CredentialAlgorithms() {
    }
}
