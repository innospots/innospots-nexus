package com.innospots.nexus.service.contract.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.audit.annotation.Audited;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected;
import com.innospots.nexus.service.contract.policy.annotation.CircuitProtected;
import com.innospots.nexus.service.contract.policy.annotation.Execution;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.policy.annotation.ServiceOperation;
import com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected;
import com.innospots.nexus.service.contract.policy.annotation.Traced;
import com.innospots.nexus.service.contract.security.annotation.PublicAccess;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;

import static org.assertj.core.api.Assertions.assertThat;

class OperationPolicyContractsTest {

    @Test
    void timeoutAndGovernanceAnnotationsUsePolicyKeys() throws Exception {
        assertThat(TimeoutProtected.class.getMethod("value").getReturnType()).isEqualTo(String.class);
        assertThat(RateLimited.class.getMethod("value").getReturnType()).isEqualTo(String.class);
        assertThat(BulkheadProtected.class.getMethod("value").getReturnType()).isEqualTo(String.class);
        assertThat(CircuitProtected.class.getMethod("value").getReturnType()).isEqualTo(String.class);
        assertThat(TimeoutProtected.class.getMethod("value").getReturnType()).isNotEqualTo(Duration.class);
    }

    @Test
    void serviceOperationIsMethodOnlyAndRequiresValue() throws Exception {
        Target target = ServiceOperation.class.getAnnotation(Target.class);
        assertThat(target.value()).containsExactly(ElementType.METHOD);
        assertThat(ServiceOperation.class.getMethod("value").getDefaultValue()).isNull();
    }

    @Test
    void permissionAndPublicAccessAreRuntimeDeclarations() throws Exception {
        assertRuntimeTypeOrMethod(RequiresPermission.class);
        assertRuntimeTypeOrMethod(PublicAccess.class);
        assertThat(PublicAccess.class.getDeclaredMethods()).isEmpty();
        Method resource = RequiresPermission.class.getMethod("resource");
        assertThat(resource.getDefaultValue()).isEqualTo("");
        assertThat(RequiresPermission.class.getMethod("value").getReturnType()).isEqualTo(String[].class);
    }

    @Test
    void auditedDefaultsToBestEffort() throws Exception {
        assertThat(Audited.class.getMethod("mode").getDefaultValue()).isEqualTo(AuditMode.BEST_EFFORT);
        assertThat(Audited.class.getMethod("resourceType").getDefaultValue()).isEqualTo("");
        assertThat(Audited.class.getMethod("snapshot").getDefaultValue()).isEqualTo("");
        assertThat(Audited.class.getMethod("action").getDefaultValue()).isNull();
    }

    @Test
    void executionAndTracedRequireExplicitValues() throws Exception {
        assertThat(Execution.class.getMethod("value").getReturnType()).isEqualTo(ExecutionMode.class);
        assertThat(Execution.class.getMethod("value").getDefaultValue()).isNull();
        assertThat(Traced.class.getMethod("value").getReturnType()).isEqualTo(String.class);
        assertThat(Traced.class.getMethod("value").getDefaultValue()).isNull();
    }

    @Test
    void operationPolicyCopiesOptionalKeysAndImmutablePermissions() {
        OperationPolicy policy = new OperationPolicy(
                Set.of("order.read"),
                "orderById",
                "http.default",
                "app.default",
                "client.orders",
                Duration.ofSeconds(2),
                true,
                "ORDER_CREATE",
                "order",
                "order.snapshot",
                AuditMode.REQUIRED,
                ResponseProfile.LEGACY);

        assertThat(policy.permissionKeys()).containsExactly("order.read");
        assertThat(policy.responseProfile()).isEqualTo(ResponseProfile.LEGACY);
        assertThat(policy.auditMode()).isEqualTo(AuditMode.REQUIRED);
    }

    private static void assertRuntimeTypeOrMethod(Class<?> type) {
        Retention retention = type.getAnnotation(Retention.class);
        Target target = type.getAnnotation(Target.class);
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(target.value()).containsExactlyInAnyOrder(ElementType.TYPE, ElementType.METHOD);
    }
}
