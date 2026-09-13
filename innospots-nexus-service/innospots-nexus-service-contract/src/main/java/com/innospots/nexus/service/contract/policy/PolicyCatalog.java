package com.innospots.nexus.service.contract.policy;

import java.util.Optional;

/**
 * Looks up resolved policies by operation identifier.
 *
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 * @see OperationDescriptor
 */
public interface PolicyCatalog {

    /**
     * Finds the policy for {@code operationId}.
     *
     * @param operationId operation identifier
     * @return policy or empty
     */
    Optional<OperationPolicy> find(String operationId);

    /**
     * Finds the static descriptor for {@code operationId}.
     *
     * @param operationId operation identifier
     * @return descriptor or empty
     */
    Optional<OperationDescriptor> descriptor(String operationId);
}
