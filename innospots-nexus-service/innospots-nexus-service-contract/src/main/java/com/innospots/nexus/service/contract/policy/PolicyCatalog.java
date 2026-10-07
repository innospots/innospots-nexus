package com.innospots.nexus.service.contract.policy;

import java.util.Optional;

/**
 * 按操作标识查找已解析策略。
 *
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 * @see OperationDescriptor
 */
public interface PolicyCatalog {

    /**
     * 查找 {@code operationId} 对应的策略。
     *
     * @param operationId 操作标识
     * @return 策略，不存在时为空
     */
    Optional<OperationPolicy> find(String operationId);

    /**
     * 查找 {@code operationId} 的静态描述符。
     *
     * @param operationId 操作标识
     * @return 描述符，不存在时为空
     */
    Optional<OperationDescriptor> descriptor(String operationId);
}
