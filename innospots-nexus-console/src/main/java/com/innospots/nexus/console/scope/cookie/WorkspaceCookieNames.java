package com.innospots.nexus.console.scope.cookie;

/**
 * 作用域选择相关的 cookie 名。
 *
 * <p>租户、工作区与项目属于「当前操作上下文」，每次请求都可能变化，
 * 因此由客户端 cookie 承载，服务端逐请求校验（见
 * {@code WorkspaceScopeResolver}），而不写进访问令牌。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
public final class WorkspaceCookieNames {

    /** 当前选中的租户标识。 */
    public static final String TENANT = "nexus_tenant_id";

    /** 当前选中的工作区标识。 */
    public static final String WORKSPACE = "nexus_workspace_id";

    /** 当前选中的项目标识。 */
    public static final String PROJECT = "nexus_project_id";

    private WorkspaceCookieNames() {
    }
}
