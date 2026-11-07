package com.innospots.nexus.console.auth.service;

/**
 * 已签发认证令牌对携带的作用域。
 *
 * <p>只描述进入令牌的身份维度：令牌类型、租户与租户成员。
 * 工作区与项目是请求期上下文，不进令牌，因此不在此记录。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param tokenType      IDENTITY 或 BUSINESS
 * @param tenantId       租户域令牌上的租户
 * @param tenantMemberId 租户域令牌上的租户成员
 */
public record AuthSessionScope(
        String tokenType,
        String tenantId,
        String tenantMemberId
) {

    /**
     * 身份令牌：尚未选择租户。
     *
     * @param tokenType 令牌类型
     * @return 无租户维度的作用域
     */
    public static AuthSessionScope identity(String tokenType) {
        return new AuthSessionScope(tokenType, null, null);
    }
}
