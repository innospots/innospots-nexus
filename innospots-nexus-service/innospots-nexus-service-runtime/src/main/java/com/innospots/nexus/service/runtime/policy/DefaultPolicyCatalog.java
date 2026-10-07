package com.innospots.nexus.service.runtime.policy;

import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.OperationDescriptor;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.PolicyCatalog;

/**
 * 基于启动时注册表的默认 {@link PolicyCatalog}。
 *
 * @author Smars
 * @date 2026/09/15
 * @see AnnotationPolicyResolver
 */
public final class DefaultPolicyCatalog implements PolicyCatalog {

    private final Map<String, OperationPolicy> policies;
    private final Map<String, OperationDescriptor> descriptors;

    /**
     * 创建目录。
     *
     * @param policies    操作标识到策略
     * @param descriptors 操作标识到描述符
     */
    public DefaultPolicyCatalog(
            Map<String, OperationPolicy> policies,
            Map<String, OperationDescriptor> descriptors) {
        this.policies = Map.copyOf(Checks.notNull(policies, "policies"));
        this.descriptors = Map.copyOf(Checks.notNull(descriptors, "descriptors"));
    }

    @Override
    public Optional<OperationPolicy> find(String operationId) {
        Checks.notBlank(operationId, "operationId");
        return Optional.ofNullable(policies.get(operationId));
    }

    @Override
    public Optional<OperationDescriptor> descriptor(String operationId) {
        Checks.notBlank(operationId, "operationId");
        return Optional.ofNullable(descriptors.get(operationId));
    }
}
