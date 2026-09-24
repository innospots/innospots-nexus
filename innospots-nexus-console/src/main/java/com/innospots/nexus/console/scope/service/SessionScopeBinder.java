package com.innospots.nexus.console.scope.service;

import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.AuthSessionScope;

/**
 * 将认证身份与作用域快照绑定到会话上下文；由 portal / platform 装配提供具体实现。
 */
public interface SessionScopeBinder {

    /**
     * 认证流程成功后绑定身份与作用域快照。
     *
     * @param user  已认证用户
     * @param scope 签发的会话作用域
     */
    void bindAfterAuth(AuthUser user, AuthSessionScope scope);

    /**
     * 为给定会话作用域绑定租户、工作区与项目快照。
     *
     * @param scope 签发的会话作用域
     */
    void bindScope(AuthSessionScope scope);

    /**
     * 从紧凑令牌声明重建会话快照。
     *
     * @param claims 解析后的令牌声明
     */
    void bindFromClaims(TokenClaims claims);

    /**
     * 清除所有绑定的会话快照与 TLC 作用域键。
     */
    void clear();
}
