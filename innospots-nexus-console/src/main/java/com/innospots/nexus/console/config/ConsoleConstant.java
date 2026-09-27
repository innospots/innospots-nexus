package com.innospots.nexus.console.config;

/**
 * 控制台模块共享常量。
 *
 * @author Smars
 * @date 2026/09/27
 */
public final class ConsoleConstant {

    /** 控制台管理 REST API 根路径前缀。 */
    public static final String API_PREFIX = "/api/nexus";

    private ConsoleConstant() {
    }

    /**
     * 拼接 {@link #API_PREFIX} 下的子路径。
     *
     * @param suffix 以 {@code /} 开头的相对路径；空串表示仅前缀
     * @return 完整 JAX-RS 路径
     */
    public static String apiPath(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return API_PREFIX;
        }
        if (!suffix.startsWith("/")) {
            return API_PREFIX + "/" + suffix;
        }
        return API_PREFIX + suffix;
    }
}
