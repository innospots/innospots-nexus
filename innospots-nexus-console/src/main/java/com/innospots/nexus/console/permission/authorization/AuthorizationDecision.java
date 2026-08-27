package com.innospots.nexus.console.permission.authorization;

/** 与框架无关的请求鉴权结果。 */
public record AuthorizationDecision(
        /** 是否允许请求继续执行。 */
        boolean allowed,
        /** 拒绝原因；允许时为空。 */
        String denyReason,
        /** 允许时交给后续适配器使用的鉴权上下文；拒绝时为空。 */
        AuthorizationContext context
) {

    /**
     * 创建允许结果。
     *
     * @param context 请求通过后形成的鉴权上下文
     * @return 允许结果
     */
    public static AuthorizationDecision allow(AuthorizationContext context) {
        return new AuthorizationDecision(true, null, context);
    }

    /**
     * 创建拒绝结果，不向请求方暴露资源目录细节。
     *
     * @param reason 拒绝原因
     * @return 拒绝结果
     */
    public static AuthorizationDecision deny(String reason) {
        return new AuthorizationDecision(false, reason, null);
    }
}
