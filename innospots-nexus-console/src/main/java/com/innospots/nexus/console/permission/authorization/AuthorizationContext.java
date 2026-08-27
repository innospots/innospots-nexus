package com.innospots.nexus.console.permission.authorization;

import java.util.List;

/** 请求鉴权通过后，传递给后续数据访问适配器的上下文。 */
public record AuthorizationContext(
        /** 当前 Workspace ID。 */
        String workspaceId,
        /** 请求头中解析出的页面 key。 */
        String pageKey,
        /** 根据页面、HTTP 方法和 URL 匹配出的 datasource key。 */
        String datasourceKey,
        /** 当前主体从角色和组织单元授权中获得的附加查询条件。 */
        List<String> constraintDefinitions
) {

    /** 复制条件集合，避免线程范围内的鉴权上下文被外部修改。 */
    public AuthorizationContext {
        constraintDefinitions = constraintDefinitions == null
                ? List.of()
                : List.copyOf(constraintDefinitions);
    }
}
