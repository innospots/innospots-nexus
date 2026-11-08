package com.innospots.nexus.console.bootstrap.domain.model;

/**
 * 控制台启动初始化的阶段，按依赖关系固定顺序执行。
 *
 * <p><b>为什么顺序不可调整：</b></p>
 * <ol>
 *   <li>{@link #CATALOG}：先把插件贡献同步为权限目录索引。后续所有授权校验
 *       都要通过目录判断资源键是否合法，目录为空时授权阶段必然失败。</li>
 *   <li>{@link #ROLE}：创建/更新角色。授权行必须挂在已存在的角色上。</li>
 *   <li>{@link #ROLE_GRANT}：写入角色资源授权。依赖目录（校验键）与角色（挂载主体）。</li>
 *   <li>{@link #USER}：创建初始用户。只依赖数据库，与角色无关。</li>
 *   <li>{@link #USER_ROLE_BINDING}：绑定用户与角色。依赖用户与角色都已就绪。</li>
 * </ol>
 *
 * <p>每个阶段实现为一个独立的 {@link com.innospots.nexus.core.bootstrap.NexusStartupTask}，
 * 由 {@link com.innospots.nexus.core.bootstrap.NexusStartup} 按 {@code order()} 排序执行，
 * 任一阶段失败即中止整个管线，避免出现"半初始化"的权限数据。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public enum BootstrapStage {

    /** 资源目录：插件贡献 → 权限目录索引。 */
    CATALOG(200, "catalog"),

    /** 角色：按配置创建或更新角色定义。 */
    ROLE(210, "role"),

    /** 角色授权：按配置写入角色持有的资源授权。 */
    ROLE_GRANT(220, "role-grant"),

    /** 用户：按配置创建初始后台用户。 */
    USER(230, "user"),

    /** 用户角色绑定：把初始用户绑定到配置声明的角色。 */
    USER_ROLE_BINDING(240, "user-role-binding");

    private final int order;
    private final String code;

    BootstrapStage(int order, String code) {
        this.order = order;
        this.code = code;
    }

    /**
     * 启动任务排序值；数值越小越先执行。
     *
     * @return 排序值
     */
    public int order() {
        return order;
    }

    /**
     * 阶段短标识，用于任务名称与日志。
     *
     * @return 阶段标识
     */
    public String code() {
        return code;
    }

    /**
     * 生成启动任务名称，形如 {@code console-bootstrap:role-grant}。
     *
     * @return 任务名称
     */
    public String taskName() {
        return "console-bootstrap:" + code;
    }
}
