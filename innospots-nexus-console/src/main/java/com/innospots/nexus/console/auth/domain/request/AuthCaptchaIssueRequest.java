package com.innospots.nexus.console.auth.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 登录前图形验证码发放请求。
 *
 * @param clientKey 客户端事务键；为空时由服务端生成并在响应中返回
 */
@Schema(name = "AuthCaptchaIssueRequest", description = "登录图形验证码发放请求")
public record AuthCaptchaIssueRequest(
        @Schema(description = "客户端事务键；为空时由服务端生成")
        String clientKey
) {
}
