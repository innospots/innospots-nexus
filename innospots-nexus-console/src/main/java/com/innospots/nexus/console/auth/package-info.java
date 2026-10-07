/**
 * 跨安全域共享的认证契约：{@link com.innospots.nexus.console.auth.api.UserDirectory}、
 * 令牌工具与平台域 {@link com.innospots.nexus.console.auth.service.AuthFacade}。
 * 租户域登录、成员关系与作用域选择归属 {@code portal.auth} / {@code portal.scope}；
 * REST 资源归属 {@code portal.auth} 与 {@code platform.auth}。
 */
package com.innospots.nexus.console.auth;
