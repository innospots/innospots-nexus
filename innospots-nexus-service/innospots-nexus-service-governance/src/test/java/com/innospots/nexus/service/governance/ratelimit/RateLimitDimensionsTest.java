package com.innospots.nexus.service.governance.ratelimit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.contract.security.PrincipalAttributeKeys;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.governance.config.RateLimitDimensionMode;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 限流维度解析测试。
 */
class RateLimitDimensionsTest {

    @Test
    void compositeIncludesCustomerWhenAttributePresent() {
        ServicePrincipal principal = new ServicePrincipal(
                "u-1",
                PrincipalType.USER,
                "realm",
                Set.of(),
                Set.of(),
                Map.of(PrincipalAttributeKeys.CUSTOMER_ID, "cust-9"));
        InvocationContext context = context(principal);
        RateLimitPolicy policy = new RateLimitPolicy(10, 1.0D, RateLimitDimensionMode.COMPOSITE);
        List<String> dimensions = RateLimitDimensions.resolve(context, policy);
        assertThat(dimensions).contains("customer:cust-9", "principal:u-1");
    }

    @Test
    void customerModeUsesOnlyCustomerDimension() {
        ServicePrincipal principal = new ServicePrincipal(
                "u-1",
                PrincipalType.USER,
                "realm",
                Set.of(),
                Set.of(),
                Map.of(PrincipalAttributeKeys.CUSTOMER_ID, "cust-9"));
        InvocationContext context = context(principal);
        RateLimitPolicy policy = new RateLimitPolicy(10, 1.0D, RateLimitDimensionMode.CUSTOMER);
        assertThat(RateLimitDimensions.resolve(context, policy)).containsExactly("customer:cust-9");
    }

    private static InvocationContext context(ServicePrincipal principal) {
        ServiceContext service = new ServiceContext(
                "req-1",
                new RequestMetadata("GET", "/ws", "/ws", Map.of(), "127.0.0.1", null),
                principal,
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        OperationPolicy operationPolicy = new OperationPolicy(
                Set.of(),
                null,
                "k",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                AuditMode.BEST_EFFORT,
                ResponseProfile.LEGACY);
        return new InvocationContext(
                "inv-1",
                "ws.message.chat",
                service,
                operationPolicy,
                new ResourceRef("websocket.message", "chat", ServiceScope.platform()));
    }
}
