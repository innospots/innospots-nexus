package com.innospots.nexus.service.runtime.security;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.contract.security.AuthenticationResult;
import com.innospots.nexus.service.contract.security.PermissionCheck;
import com.innospots.nexus.service.contract.security.PermissionDecision;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 认证与授权拦截器链行为测试。
 */
class SecurityInterceptorsTest {

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();

    @Test
    void authenticationCoordinatorInstallsAuthenticatedPrincipal() {
        ServicePrincipal user = new ServicePrincipal(
                "user-1", PrincipalType.USER, "local", Set.of(), Set.of(), Map.of());
        SecurityProvider provider = new SecurityProvider() {
            @Override
            public String id() {
                return "host";
            }

            @Override
            public CompletionStage<AuthenticationResult> authenticate(ServiceContext context) {
                return CompletableFuture.completedFuture(new AuthenticationResult(user, ServiceScope.platform(), null));
            }
        };
        InvocationEngine engine = new InvocationEngine(
                List.of(new AuthenticationCoordinator(provider, contexts)),
                contexts);

        engine.invokeSync(protectedContext(), () -> {
            assertThat(contexts.requireCurrent().security().id()).isEqualTo("user-1");
            return "ok";
        });

        assertThat(contexts.current()).isEmpty();
    }

    @Test
    void authorizationInterceptorRejectsMissingPermission() {
        PermissionProvider provider = (principal, scope, check) ->
                CompletableFuture.completedFuture(PermissionDecision.deny("missing"));
        InvocationEngine engine = new InvocationEngine(
                List.of(new AuthorizationInterceptor(provider, contexts)),
                contexts);

        assertThatThrownBy(() -> engine.invokeSync(protectedContext(), () -> "nope"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void publicOperationSkipsAuthenticationAndAuthorization() {
        AtomicProvider auth = new AtomicProvider();
        AtomicPermissionProvider permission = new AtomicPermissionProvider();
        InvocationEngine engine = new InvocationEngine(
                List.of(
                        new AuthenticationCoordinator(auth, contexts),
                        new AuthorizationInterceptor(permission, contexts)),
                contexts);

        engine.invokeSync(publicContext(), () -> "ok");

        assertThat(auth.calls()).isZero();
        assertThat(permission.calls()).isZero();
    }

    private InvocationContext protectedContext() {
        ServiceContext service = baseContext();
        OperationPolicy policy = new OperationPolicy(
                Set.of("order.read"),
                null,
                null,
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
                "op-1",
                service,
                policy,
                new ResourceRef("order", "1", ServiceScope.platform()));
    }

    private InvocationContext publicContext() {
        ServiceContext service = baseContext();
        OperationPolicy policy = new OperationPolicy(
                Set.of(),
                null,
                null,
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
                "inv-2",
                "op-public",
                service,
                policy,
                new ResourceRef("health", "1", ServiceScope.platform()));
    }

    private static ServiceContext baseContext() {
        return new ServiceContext(
                "req-1",
                new RequestMetadata("GET", "/orders", "/orders", Map.of(), "127.0.0.1", null),
                ServicePrincipal.anonymous("local"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
    }

    private static final class AtomicProvider implements SecurityProvider {

        private int calls;

        @Override
        public String id() {
            return "host";
        }

        @Override
        public CompletionStage<AuthenticationResult> authenticate(ServiceContext context) {
            calls++;
            return CompletableFuture.completedFuture(new AuthenticationResult(
                    new ServicePrincipal("user-1", PrincipalType.USER, "local", Set.of(), Set.of(), Map.of()),
                    ServiceScope.platform(),
                    Instant.now().plusSeconds(60)));
        }

        private int calls() {
            return calls;
        }
    }

    private static final class AtomicPermissionProvider implements PermissionProvider {

        private int calls;

        @Override
        public CompletionStage<PermissionDecision> authorize(
                ServicePrincipal principal,
                ServiceScope scope,
                PermissionCheck check) {
            calls++;
            return CompletableFuture.completedFuture(PermissionDecision.allow());
        }

        private int calls() {
            return calls;
        }
    }
}
