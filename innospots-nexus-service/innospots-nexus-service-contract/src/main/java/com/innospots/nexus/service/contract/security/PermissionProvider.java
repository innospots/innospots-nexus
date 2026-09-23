package com.innospots.nexus.service.contract.security;

import java.util.concurrent.CompletionStage;

/**
 * 根据 {@link PermissionCheck} 授权主体。
 *
 * @author Smars
 * @date 2026/09/13
 * @see PermissionCheck
 * @see PermissionDecision
 */
public interface PermissionProvider {

    /**
     * 在 {@code scope} 中为 {@code check} 授权 {@code principal}。
     *
     * @param principal 调用方
     * @param scope     资源作用域
     * @param check     权限请求
     * @return 决策
     */
    CompletionStage<PermissionDecision> authorize(
            ServicePrincipal principal,
            ServiceScope scope,
            PermissionCheck check);
}
