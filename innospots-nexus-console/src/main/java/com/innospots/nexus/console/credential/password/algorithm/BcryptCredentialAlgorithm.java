package com.innospots.nexus.console.credential.password.algorithm;

import com.innospots.nexus.base.util.CryptoUtils;

/**
 * 默认密码算法：BCrypt 单向哈希，标识 {@link CredentialAlgorithms#BCRYPT_V1}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CredentialAlgorithmRegistry
 */
public final class BcryptCredentialAlgorithm implements CredentialAlgorithm {

    @Override
    public String algorithmId() {
        return CredentialAlgorithms.BCRYPT_V1;
    }

    @Override
    public EncodedCredential encode(String rawProof) {
        return new EncodedCredential(algorithmId(), CryptoUtils.encryptPassword(rawProof), null);
    }

    @Override
    public boolean verify(String verifier, String verifierParams, String rawProof) {
        // verifierParams 对 BCrypt 无意义；哈希已嵌入 verifier 字符串
        return CryptoUtils.matchesPassword(rawProof, verifier);
    }
}
