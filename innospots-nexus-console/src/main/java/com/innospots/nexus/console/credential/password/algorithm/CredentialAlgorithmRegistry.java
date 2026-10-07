package com.innospots.nexus.console.credential.password.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 按算法 ID 解析 {@link CredentialAlgorithm}；默认注册 bcrypt 密码哈希。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CredentialAlgorithms
 * @see com.innospots.nexus.console.credential.password.operator.UserCredentialOperator
 */
public final class CredentialAlgorithmRegistry {

    private final Map<String, CredentialAlgorithm> algorithms;

    /**
     * 注册自定义算法表（单测或多算法环境）。
     *
     * @param algorithms 算法 ID 到实现的映射
     */
    public CredentialAlgorithmRegistry(Map<String, CredentialAlgorithm> algorithms) {
        this.algorithms = Map.copyOf(new LinkedHashMap<>(algorithms));
    }

    /**
     * 内置 BCrypt 密码算法的默认注册表。
     * @return 仅含 {@link CredentialAlgorithms#BCRYPT_V1} 的注册表
     */
    public static CredentialAlgorithmRegistry defaults() {
        BcryptCredentialAlgorithm bcrypt = new BcryptCredentialAlgorithm();
        return new CredentialAlgorithmRegistry(Map.of(bcrypt.algorithmId(), bcrypt));
    }

    /**
     * 按持久化算法 ID 解析实现，未知 ID 时抛出业务异常。
     * <p>调用场景：加载历史凭据行后校验或重编码。</p>
     *
     * @param algorithmId 库中 {@code algorithm_id}
     * @return 对应算法实现
     * @throws NexusException 未知算法 ID
     */
    public CredentialAlgorithm require(String algorithmId) {
        Objects.requireNonNull(algorithmId, "algorithmId");
        CredentialAlgorithm algorithm = algorithms.get(algorithmId);
        if (algorithm == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER.fullCode(), "Unknown credential algorithm");
        }
        return algorithm;
    }

    /**
     * 当前环境的默认密码哈希算法（BCrypt v1）。
     * <p>调用场景：新用户注册写入默认 {@link CredentialAlgorithms#BCRYPT_V1}。</p>
     *
     * @return 默认 {@link CredentialAlgorithm}
     */
    public CredentialAlgorithm defaultAlgorithm() {
        return require(CredentialAlgorithms.BCRYPT_V1);
    }
}
