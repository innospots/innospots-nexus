package com.innospots.nexus.console.permission.authorization;

/**
 * 由 Filter 或 REST 适配器提取的、与框架无关的请求鉴权数据。
 *
 * @author Smars
 * @date 2026/09/13
 */
public record AuthorizationRequest(
        /**
         * 当前 Workspace ID。
         */
        String workspaceId,
        /**
         * catalog 授权资源键，格式 {@code grantName:domainKey.moduleKey}。
         */
        String resourceKey,
        /**
         * 当前登录主体及其角色、组织单元信息。
         */
        AuthorizationSubject subject
) {
}
