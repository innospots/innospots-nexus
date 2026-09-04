package com.innospots.nexus.console.permission.authorization;

import java.util.Optional;

/** 从当前请求上下文解析鉴权主体的端口。 */
public interface AuthorizationSubjectResolver {

    /**
     * 解析当前请求的鉴权主体。
     *
     * @return 已认证主体；未登录时返回空
     */
    Optional<AuthorizationSubject> resolve();
}
