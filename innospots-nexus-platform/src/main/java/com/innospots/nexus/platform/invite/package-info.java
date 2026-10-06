/**
 * 平台用户邀请：管理端发邀请与公开链接/邀请码激活（INVITE 自助注册模式）。
 *
 * <p>管理员邀请（{@code POST /api/platform/invites}）可在任意注册模式下使用；
 * 注册页邀请码激活受 {@link com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode#INVITE} 约束，
 * 邮件/链接 token 接受流程不受模式限制（邀请单仍有效即可）。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint
 */
package com.innospots.nexus.platform.invite;
