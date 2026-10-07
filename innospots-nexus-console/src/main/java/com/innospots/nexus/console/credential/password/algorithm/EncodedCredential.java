package com.innospots.nexus.console.credential.password.algorithm;

/**
 * 算法产出的 opaque 验证材料，供 {@link com.innospots.nexus.console.credential.password.operator.UserCredentialOperator} 写入库表。
 *
 * @param algorithm      算法标识，对应 {@code algorithm_id}
 * @param verifier       不透明验证载荷
 * @param verifierParams 算法私有参数（可为 null；console 不解析语义）
 * @author Smars
 * @date 2026/09/13
 * @see CredentialAlgorithm#encode(String)
 */
public record EncodedCredential(String algorithm, String verifier, String verifierParams) {
}
