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

    /**
     * 公共开放 REST API 根路径前缀（无需鉴权）。
     * <p>运营域、租户域等业务域路径不在此定义，由各业务模块自行维护。</p>
     */
    public static final String PUBLIC_API_PREFIX = "/api/public";

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

    /**
     * 拼接 {@link #PUBLIC_API_PREFIX} 下的子路径。
     *
     * @param suffix 以 {@code /} 开头的相对路径；空串表示仅前缀
     * @return 完整 JAX-RS 路径
     */
    public static String publicPath(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return PUBLIC_API_PREFIX;
        }
        if (!suffix.startsWith("/")) {
            return PUBLIC_API_PREFIX + "/" + suffix;
        }
        return PUBLIC_API_PREFIX + suffix;
    }
}
