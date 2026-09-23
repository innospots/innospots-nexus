package com.innospots.nexus.console.auth.domain.request;

/**
 * 登录前图形验证码发放请求。
 *
 * @param clientKey 客户端事务键；为空时由服务端生成并在响应中返回
 */
public record AuthCaptchaIssueRequest(String clientKey) {
}
