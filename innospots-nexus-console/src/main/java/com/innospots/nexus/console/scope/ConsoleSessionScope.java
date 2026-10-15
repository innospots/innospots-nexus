package com.innospots.nexus.console.scope;

/**
 * 控制台登录会话的归属层级（与数据 {@link RoleOwnerType} 对齐，表示「当前操作上下文」）。
 */
public enum ConsoleSessionScope {

    PLATFORM,

    TENANT,

    WORKSPACE;

    /**
     * 解析配置字符串；空白或未知值返回 {@code null}。
     *
     * @param value 配置值
     * @return 枚举或 null
     */
    public static ConsoleSessionScope parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (ConsoleSessionScope scope : values()) {
            if (scope.name().equalsIgnoreCase(normalized)) {
                return scope;
            }
        }
        return null;
    }
}
