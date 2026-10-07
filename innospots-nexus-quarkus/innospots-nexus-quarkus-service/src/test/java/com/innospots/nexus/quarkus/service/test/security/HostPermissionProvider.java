package com.innospots.nexus.quarkus.service.test.security;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.contract.security.PermissionCheck;
import com.innospots.nexus.service.contract.security.PermissionDecision;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * 测试宿主权限提供者。
 */
@ApplicationScoped
public final class HostPermissionProvider implements PermissionProvider {

    @Override
    public CompletionStage<PermissionDecision> authorize(
            ServicePrincipal principal,
            ServiceScope scope,
            PermissionCheck check) {
        if ("adapter-user".equals(principal.id()) && check.permissions().contains("adapter.secure")) {
            return CompletableFuture.completedFuture(PermissionDecision.allow());
        }
        return CompletableFuture.completedFuture(PermissionDecision.deny("forbidden"));
    }
}
