/**
 * 平台用户（{@code nx_pl_user}）持久化与管理端 REST。
 *
 * <p>本域仅承载<strong>平台运营账号</strong>（{@code nx_pl_user}，{@code SecurityRealm.PLATFORM}），
 * 与 portal 租户成员用户分离。{@code nx_pl_user_oauth} 当前仅为占位表，无 REST。</p>
 *
 * <p>{@link com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint} 经 {@link com.innospots.nexus.platform.user.service.PlatformUserService}
 * 提供管理员直创与档案维护；{@link com.innospots.nexus.platform.user.operator.PlatformUserOperator} 仅负责
 * {@code nx_pl_user} 持久化。自助开户由 {@code access}、{@code invite} 经 Service 编排。</p>
 *
 * <p>非空 email/mobile 在创建与更新时做业务层唯一校验；登录解析顺序为 loginName → email → mobile
 * （见 {@link com.innospots.nexus.platform.auth.support.PlatformUserIdentityResolver}）。
 * 管理端 {@code PUT …/status} 仅允许设为 {@code ACTIVE} 或 {@code DISABLED}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus
 * @see com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory
 */
package com.innospots.nexus.platform.user;
