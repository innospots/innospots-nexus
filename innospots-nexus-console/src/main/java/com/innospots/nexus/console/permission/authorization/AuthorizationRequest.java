package com.innospots.nexus.console.permission.authorization;

/** 由 Filter 或 REST 适配器提取的、与框架无关的请求鉴权数据。 */
public record AuthorizationRequest(
        /** 当前 Workspace ID。 */
        String workspaceId,
        /** 实际 HTTP 方法。 */
        String method,
        /** 实际请求路径，可以带查询字符串。 */
        String path,
        /** 请求头中的页面 key。 */
        String pageKey,
        /** 当前登录主体及其角色、组织单元信息。 */
        AuthorizationSubject subject
) {
}
