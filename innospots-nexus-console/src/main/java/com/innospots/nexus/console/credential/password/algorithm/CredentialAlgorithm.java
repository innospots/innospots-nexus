package com.innospots.nexus.console.credential.password.algorithm;

/**
 * 可插拔凭据编码与验证；持久化层不解析 {@code verifier} 结构，由 {@link CredentialAlgorithmRegistry} 路由。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.algorithm.BcryptCredentialAlgorithm
 * @see CredentialAlgorithmRegistry
 */
public interface CredentialAlgorithm {

    /**
     * 稳定算法标识，如 {@link CredentialAlgorithms#BCRYPT_V1}。
     * <p>调用场景：写入 {@code nx_user_credential.algorithm_id} 与按 ID 解析实现。</p>
     *
     * @return 持久化的算法 ID
     */
    String algorithmId();

    /**
     * 从注册明文生成存储用验证材料。
     * <p>调用场景：用户注册或管理员重置密码时 {@link com.innospots.nexus.console.credential.password.operator.UserCredentialOperator} 编码新密码。</p>
     *
     * @param rawProof 明文密码或证明
     * @return 算法 ID 与 verifier 载荷
     */
    EncodedCredential encode(String rawProof);

    /**
     * 校验明文证明是否匹配已存材料。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.password.service.CredentialService#authenticate}。</p>
     *
     * @param verifier       库中主验证字段
     * @param verifierParams 算法扩展参数，可为 null
     * @param rawProof       用户提交的明文
     * @return 匹配时为 {@code true}
     */
    boolean verify(String verifier, String verifierParams, String rawProof);
}
