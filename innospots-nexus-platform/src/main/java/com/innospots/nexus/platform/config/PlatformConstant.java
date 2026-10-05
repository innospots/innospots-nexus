package com.innospots.nexus.platform.config;

/**
 * 运营管理平台（platform）REST API 路径常量。
 *
 * @author Smars
 * @date 2026/09/29
 */
public final class PlatformConstant {

    /** 运营管理平台 REST API 根路径前缀。 */
    public static final String API_PREFIX = "/platform";

    /** 运营管理平台认证：{@value} */
    public static final String AUTH_PATH = API_PREFIX + "/auth";

    /** 运营管理平台租户生命周期：{@value} */
    public static final String TENANTS_PATH = API_PREFIX + "/tenants";

    /** 运营管理平台用户：{@value} */
    public static final String USERS_PATH = API_PREFIX + "/users";

    private PlatformConstant() {
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
