/**
 * 平台用户（{@code nx_pl_user}）持久化与管理端 REST。
 *
 * <p>{@link com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint} 经 {@link com.innospots.nexus.platform.user.service.PlatformUserService}
 * 提供管理员直创与档案维护；{@link com.innospots.nexus.platform.user.operator.PlatformUserOperator} 仅负责
 * {@code nx_pl_user} 持久化。自助开户由 {@code access}、{@code invite} 经 Service 编排。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus
 * @see com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory
 */
package com.innospots.nexus.platform.user;
