package com.innospots.nexus.service.runtime.policy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected;
import com.innospots.nexus.service.contract.security.annotation.PublicAccess;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 注解策略解析与 PublicAccess 冲突规则测试。
 */
class AnnotationPolicyResolverTest {

    private final AnnotationPolicyResolver resolver = new AnnotationPolicyResolver();

    @Test
    void resolvesMergedPermissionsAndGovernanceKeys() throws Exception {
        Method method = SecuredResource.class.getMethod("create");

        OperationPolicy policy = resolver.resolve(SecuredResource.class, method);

        assertThat(policy.permissionKeys()).containsExactly("order.read", "order.write");
        assertThat(policy.rateLimitKey()).isEqualTo("http.default");
        assertThat(policy.audited()).isTrue();
        assertThat(policy.auditAction()).isEqualTo("ORDER_CREATE");
        assertThat(policy.auditMode()).isEqualTo(AuditMode.REQUIRED);
    }

    @Test
    void publicAccessClearsPermissions() throws Exception {
        Method method = PublicResource.class.getMethod("health");

        OperationPolicy policy = resolver.resolve(PublicResource.class, method);

        assertThat(policy.permissionKeys()).isEmpty();
    }

    @Test
    void resolvesTimeoutPolicyKey() throws Exception {
        Method method = TimeoutResource.class.getMethod("slow");

        OperationPolicy policy = resolver.resolve(TimeoutResource.class, method);

        assertThat(policy.timeoutPolicyKey()).isEqualTo("payment.client");
        assertThat(policy.timeout()).isNull();
    }

    @Test
    void publicAccessConflictsWithMethodPermission() throws Exception {
        Method method = ConflictingResource.class.getMethod("conflict");

        assertThatThrownBy(() -> resolver.validate(ConflictingResource.class, method))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }

    @RateLimited("http.default")
    @RequiresPermission("order.read")
    private static class SecuredResource {

        @RequiresPermission("order.write")
        @com.innospots.nexus.service.contract.audit.annotation.Audited(
                action = "ORDER_CREATE",
                mode = AuditMode.REQUIRED)
        public void create() {
        }
    }

    private static class PublicResource {

        @PublicAccess
        public void health() {
        }
    }

    private static class ConflictingResource {

        @PublicAccess
        @RequiresPermission("order.read")
        public void conflict() {
        }
    }

    private static class TimeoutResource {

        @TimeoutProtected("payment.client")
        public void slow() {
        }
    }
}
