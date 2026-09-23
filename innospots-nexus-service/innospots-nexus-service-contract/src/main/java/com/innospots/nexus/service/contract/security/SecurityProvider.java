package com.innospots.nexus.service.contract.security;

import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * 认证预认证 {@link ServiceContext} 的宿主 IAM 适配器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuthenticationResult
 * @see PermissionProvider
 */
public interface SecurityProvider {

    /**
     * 返回提供者标识。
     *
     * @return 稳定标识
     */
    String id();

    /**
     * 认证当前请求。凭证保留在原生请求作用域。
     *
     * @param context 预认证上下文
     * @return 认证结果
     */
    CompletionStage<AuthenticationResult> authenticate(ServiceContext context);
}
