package com.innospots.nexus.spring.service.test.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.springframework.stereotype.Component;

import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.AuthenticationResult;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 测试宿主安全提供者。
 */
@Component
public final class HostSecurityProvider implements SecurityProvider {

    @Override
    public String id() {
        return "host";
    }

    @Override
    public CompletionStage<AuthenticationResult> authenticate(ServiceContext context) {
        String authorization = header(context, StandardHeaders.AUTHORIZATION);
        if ("Bearer adapter-user".equals(authorization)) {
            return CompletableFuture.completedFuture(new AuthenticationResult(
                    new ServicePrincipal(
                            "adapter-user",
                            PrincipalType.USER,
                            "adapter",
                            Set.of("adapter-user"),
                            Set.of("adapter.secure"),
                            Map.of()),
                    ServiceScope.platform(),
                    Instant.now().plusSeconds(3600)));
        }
        return CompletableFuture.completedFuture(new AuthenticationResult(
                new ServicePrincipal(
                        "guest",
                        PrincipalType.USER,
                        "adapter",
                        Set.of(),
                        Set.of(),
                        Map.of()),
                ServiceScope.platform(),
                Instant.now().plusSeconds(3600)));
    }

    private static String header(ServiceContext context, String name) {
        List<String> values = context.request().headers().get(name.toLowerCase());
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }
}
