package com.innospots.nexus.console.jaxrs.filter;

/**
 * 客户端作用域状态提供者。
 *
 * <p>{@link WorkspaceScopeSessionFilter} 只负责把解析结果绑定到会话；「客户端选了什么」
 * 「该用户是否真能进入」属于业务方语义，由实现方决定。这样 console 保持业务中立。</p>
 *
 * <p>实现方必须自行校验：客户端声明的标识不可直接采信，必须重新校验访问权后返回快照；
 * 无权或未声明时返回 {@code null}。</p>
 */
public interface WorkspaceScopeResolver {

    /**
     * 解析当前请求应绑定的作用域。
     *
     * @param cookieHeader 原始 {@code Cookie} 请求头；无 cookie 时为 {@code null}
     * @return 待绑定快照；无需绑定或校验不通过时返回 {@code null}
     */
    ResolvedWorkspaceScope resolve(String cookieHeader);

    /**
     * 作用域快照：租户 → 工作区 → 项目（可选）。
     *
     * @param tenantId      租户标识
     * @param tenantCode    租户编码；展示与快照用，可为空
     * @param tenantName    租户名称；展示与快照用，可为空
     * @param workspaceId   工作区标识
     * @param workspaceCode 工作区编码
     * @param workspaceName 工作区名称
     * @param description   工作区说明；可为空
     * @param projectId     项目标识；未声明时为 {@code null}
     * @param projectCode   项目编码；可为空
     * @param projectName   项目名称；可为空
     */
    record ResolvedWorkspaceScope(
            String tenantId,
            String tenantCode,
            String tenantName,
            String workspaceId,
            String workspaceCode,
            String workspaceName,
            String description,
            String projectId,
            String projectCode,
            String projectName
    ) {

        /**
         * 校验最小可用字段。
         *
         * @return 租户与工作区标识是否齐备
         */
        public boolean isValid() {
            return !isBlank(tenantId) && !isBlank(workspaceId);
        }

        private static boolean isBlank(String value) {
            return value == null || value.isBlank();
        }
    }
}
