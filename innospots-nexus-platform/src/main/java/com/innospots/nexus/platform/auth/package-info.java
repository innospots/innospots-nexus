/**
 * 运营管理平台认证 REST 与 {@link com.innospots.nexus.console.auth} 端口适配。
 *
 * <p>匿名登录与密码重置在 {@code /api/platform/public/auth}；刷新、登出与改密在 {@code /api/platform/auth}。
 * {@code /auth/refresh} 在 Filter 层放行，业务层校验 body 中的 refresh token；{@code /auth/logout} 与改密需 Bearer。
 * 登出 MVP 不服务端吊销 refresh（见 {@link com.innospots.nexus.console.auth.service.AuthFacade#logout}）。
 * 不含自助注册（见 {@code registration}、{@code access}、{@code invite}）。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.auth.service.PlatformAuthService
 * @see com.innospots.nexus.platform.auth.service.PlatformPasswordService
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformPublicAuthEndpoint
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformPublicPasswordResetEndpoint
 * @see com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory
 * @see com.innospots.nexus.platform.user.operator.PlatformUserOperator
 */
package com.innospots.nexus.platform.auth;
