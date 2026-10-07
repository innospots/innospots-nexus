/**
 * 平台用户邀请：管理端发邀请与公开链接/邀请码激活（INVITE 自助注册模式）。
 *
 * <p>本域仅服务<strong>平台 IAM 用户</strong>（{@code nx_pl_user}），与 portal 租户成员邀请无关。</p>
 *
 * <p>管理员邀请（{@code POST /api/platform/invites}）可在任意注册模式下使用；
 * 注册页邀请码激活受 {@link com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode#INVITE} 约束，
 * 邮件/链接 token 接受流程不受模式限制（邀请单仍有效即可）。</p>
 *
 * <p>{@code defaultRoleCodes} 非空时，接受邀请后绑定 PLATFORM 作用域角色；为空则不自动赋角。
 * 登录名冲突时开通失败且邀请单保持待接受。</p>
 *
 * <p>审计与访问日志不得记录完整邀请 token；仅使用邀请 ID 或掩码。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint
 */
package com.innospots.nexus.platform.invite;
