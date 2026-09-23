package com.innospots.nexus.service.governance.ratelimit;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.security.PrincipalAttributeKeys;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.governance.config.RateLimitDimensionMode;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

/**
 * 从调用上下文解析限流维度标签（供拦截器与测试复用）。
 */
public final class RateLimitDimensions {

    private static final String CUSTOMER_PREFIX = "customer:";

    private RateLimitDimensions() {
    }

    /**
     * 按策略维度模式构造标签列表。
     *
     * @param invocation 调用上下文
     * @param policy     已解析的限流策略参数
     * @return 非 {@code null} 维度列表；空列表表示仅按 policyKey 单桶
     */
    public static List<String> resolve(InvocationContext invocation, RateLimitPolicy policy) {
        Checks.notNull(invocation, "invocation");
        Checks.notNull(policy, "policy");
        if (policy.dimensionMode() == RateLimitDimensionMode.CUSTOMER) {
            return customerOnly(invocation);
        }
        return composite(invocation);
    }

    private static List<String> customerOnly(InvocationContext invocation) {
        String customerId = customerId(invocation.service().security());
        if (customerId == null) {
            return List.of();
        }
        return List.of(CUSTOMER_PREFIX + customerId);
    }

    private static List<String> composite(InvocationContext invocation) {
        List<String> dimensions = new ArrayList<>(6);
        dimensions.add("operation:" + invocation.operationId());
        dimensions.add("principal:" + invocation.service().security().id());
        dimensions.add("realm:" + invocation.service().security().realm());
        if (invocation.service().scope().tenantId() != null) {
            dimensions.add("tenant:" + invocation.service().scope().tenantId());
        }
        String customerId = customerId(invocation.service().security());
        if (customerId != null) {
            dimensions.add(CUSTOMER_PREFIX + customerId);
        }
        return List.copyOf(dimensions);
    }

    private static String customerId(ServicePrincipal principal) {
        Checks.notNull(principal, "principal");
        String value = principal.attributes().get(PrincipalAttributeKeys.CUSTOMER_ID);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
