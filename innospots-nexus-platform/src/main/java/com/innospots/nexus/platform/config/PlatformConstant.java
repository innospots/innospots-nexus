package com.innospots.nexus.platform.config;

/**
 * 运营管理平台（platform）REST API 路径常量。
 *
 * <p>管理端路径使用 {@link #API_PREFIX}；匿名自助注册与策略查询使用 {@link #PUBLIC_PREFIX}。
 * 三种自助注册与 {@link com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode}
 * 的对应关系见 {@code platform.registration} 与 {@code platform.settings} 包说明。</p>
 *
 * @author Smars
 * @date 2026/09/29
 * @see com.innospots.nexus.platform.settings.endpoint.PlatformRegistrationModeSettingEndpoint
 */
public final class PlatformConstant {

    /** 运营管理平台 REST API 根路径前缀。 */
    public static final String API_PREFIX = "/api/platform";

    /** 无需登录的公开 API 前缀。 */
    public static final String PUBLIC_PREFIX = API_PREFIX + "/public";

    /** 运营管理平台认证（已登录会话）：{@value} */
    public static final String AUTH_PATH = API_PREFIX + "/auth";

    /** 运营管理平台匿名认证（登录、验证码）：{@value} */
    public static final String PUBLIC_AUTH_PATH = PUBLIC_PREFIX + "/auth";

    /** 运营管理平台令牌刷新（与 {@link #AUTH_PATH} 同前缀）：{@value} */
    public static final String AUTH_REFRESH_PATH = AUTH_PATH + "/refresh";

    /** 运营管理平台匿名密码重置：{@value} */
    public static final String PUBLIC_AUTH_PASSWORD_PATH = PUBLIC_AUTH_PATH + "/password";

    /** 运营管理平台租户生命周期：{@value} */
    public static final String TENANTS_PATH = API_PREFIX + "/tenants";

    /** 运营管理平台用户：{@value} */
    public static final String USERS_PATH = API_PREFIX + "/users";

    /** 平台用户邀请（管理端）：{@value} */
    public static final String INVITES_PATH = API_PREFIX + "/invites";

    /** 邀请注册（公开）：{@value} */
    public static final String PUBLIC_INVITES_PATH = PUBLIC_PREFIX + "/invites";

    /** 用户注册（公开根路径）：{@value} */
    public static final String PUBLIC_REGISTRATION_PATH = PUBLIC_PREFIX + "/registration";

    /** 完全开放注册（公开）：{@value} */
    public static final String PUBLIC_OPEN_REGISTRATION_PATH = PUBLIC_REGISTRATION_PATH + "/open";

    /** 主动注册 / 待审批（公开提交）：{@value} */
    public static final String PUBLIC_ACCESS_REGISTRATION_PATH = PUBLIC_REGISTRATION_PATH + "/access-requests";

    /** 主动注册审批（管理端）：{@value} */
    public static final String REGISTRATION_ACCESS_REQUESTS_PATH = API_PREFIX + "/registration/access-requests";

    /** 运营平台设置（管理端根路径）：{@value} */
    public static final String SETTINGS_PATH = API_PREFIX + "/settings";

    /** 自助注册模式设置（管理端）：{@value} */
    public static final String SETTINGS_REGISTRATION_MODE_PATH = SETTINGS_PATH + "/registration-mode";

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
