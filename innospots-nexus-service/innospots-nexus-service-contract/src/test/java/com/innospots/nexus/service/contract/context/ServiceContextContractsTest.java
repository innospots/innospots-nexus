package com.innospots.nexus.service.contract.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 服务上下文契约的不可变性与便捷访问行为测试。
 */
class ServiceContextContractsTest {

    @Test
    void rejectsNullComponents() {
        assertThatThrownBy(() -> new ServiceContext(
                        null,
                        request(),
                        ServicePrincipal.anonymous("local"),
                        ServiceScope.platform(),
                        TraceSnapshot.empty(),
                        CancellationToken.none(),
                        Deadline.unlimited(),
                        ContextAttributes.empty()))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
    }

    @Test
    void copiesHeadersToLowercaseAndFreezesValues() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Request-Id", List.of("abc"));
        RequestMetadata metadata = new RequestMetadata("GET", "/orders", null, headers, "127.0.0.1", "client-1");

        assertThat(metadata.headers()).containsOnlyKeys("x-request-id");
        assertThat(metadata.headers().get("x-request-id")).containsExactly("abc");
        assertThatThrownBy(() -> metadata.headers().put("y", List.of("z")))
                .isInstanceOf(UnsupportedOperationException.class);

        headers.put("Authorization", List.of("secret"));
        assertThat(metadata.headers()).doesNotContainKey("authorization");
    }

    @Test
    void anonymousPrincipalUsesFixedId() {
        ServicePrincipal principal = ServicePrincipal.anonymous("local");
        assertThat(principal.id()).isEqualTo("anonymous");
        assertThat(principal.type()).isEqualTo(PrincipalType.ANONYMOUS);
        assertThat(principal.realm()).isEqualTo("local");
    }

    @Test
    void authenticatedPrincipalRequiresIdAndRealm() {
        assertThatThrownBy(() -> new ServicePrincipal(
                        "",
                        PrincipalType.USER,
                        "local",
                        Set.of(),
                        Set.of(),
                        Map.of()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> new ServicePrincipal(
                        "user-1",
                        PrincipalType.USER,
                        " ",
                        Set.of(),
                        Set.of(),
                        Map.of()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void principalCollectionsAreImmutableCopies() {
        Set<String> roles = new HashSet<>(Set.of("admin"));
        ServicePrincipal principal = new ServicePrincipal(
                "user-1",
                PrincipalType.USER,
                "local",
                roles,
                Set.of("order.read"),
                Map.of("k", "v"));

        roles.add("hacked");
        assertThat(principal.roles()).containsExactly("admin");
        assertThatThrownBy(() -> principal.roles().add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> principal.permissions().add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> principal.attributes().put("a", "b"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void scopeEnforcesTenantWorkspaceProjectHierarchy() {
        assertThatThrownBy(() -> new ServiceScope(null, "ws-1", null))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> new ServiceScope("t-1", null, "p-1"))
                .isInstanceOf(NexusException.class);
        ServiceScope scope = new ServiceScope("t-1", "ws-1", "p-1");
        assertThat(scope.tenantId()).isEqualTo("t-1");
    }

    @Test
    void contextConvenienceAccessorsReadSecurityAndScope() {
        ServicePrincipal principal = new ServicePrincipal(
                "user-1", PrincipalType.USER, "local", Set.of(), Set.of(), Map.of());
        ServiceContext context = new ServiceContext(
                "req-1",
                request(),
                principal,
                new ServiceScope("t-1", "ws-1", null),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());

        assertThat(context.principal()).isSameAs(principal);
        assertThat(context.clientId()).contains("client-1");
        assertThat(context.tenantId()).contains("t-1");
        assertThat(context.workspaceId()).contains("ws-1");
        assertThat(context.projectId()).isEmpty();
        assertThat(context.trace().traceId()).isEmpty();
        assertThat(context.deadline().isUnlimited()).isTrue();
    }

    @Test
    void accessorRequireCurrentUsesServiceStatus() {
        ServiceContextAccessor accessor = java.util.Optional::empty;
        assertThatThrownBy(accessor::requireCurrent)
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.CONTEXT_UNAVAILABLE.fullCode());
    }

    private static RequestMetadata request() {
        return new RequestMetadata("GET", "/orders", "/orders", Map.of(), "127.0.0.1", "client-1");
    }
}
