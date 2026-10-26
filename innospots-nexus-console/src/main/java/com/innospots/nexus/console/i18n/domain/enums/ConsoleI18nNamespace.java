package com.innospots.nexus.console.i18n.domain.enums;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Console UI 语言包命名空间。每个值对应
 * {@code i18n/console/<namespace>/messages*.properties}。
 *
 * @author Smars
 * @date 2026/10/26
 * @see com.innospots.nexus.console.i18n.loader.MessageBundleLoader
 */
@Schema(name = "ConsoleI18nNamespace", description = "控制台语言包命名空间")
public enum ConsoleI18nNamespace {

    /** 跨模块共享文案与 Sitemap 外壳。 */
    COMMON("common"),
    /** 登录、找回密码与账号页。 */
    AUTH("auth"),
    /** 开放注册与审批入口。 */
    ACCESS("access"),
    /** 邀请接受与邀请码注册。 */
    INVITE("invite"),
    /** 导航菜单配置。 */
    MENU("menu"),
    /** 角色与授权。 */
    ROLE("role"),
    /** 数据字典。 */
    DICTIONARY("dictionary"),
    /** 审计日志。 */
    AUDIT("audit"),
    /** 插件管理。 */
    PLUGIN("plugin"),
    /** 平台用户与注册审批。 */
    PLATFORM_USER("platform-user"),
    /** 租户与组织管理。 */
    ORGANIZATION("organization");

    private final String code;

    ConsoleI18nNamespace(String code) {
        this.code = code;
    }

    /**
     * 返回稳定 namespace 编码。
     *
     * @return namespace 编码
     */
    public String code() {
        return code;
    }

    /**
     * 按稳定编码查找 namespace。
     *
     * @param code namespace 编码
     * @return 匹配的 namespace
     */
    public static Optional<ConsoleI18nNamespace> fromCode(String code) {
        String normalized = code == null ? "" : code.trim();
        return Arrays.stream(values())
                .filter(value -> value.code.equals(normalized))
                .findFirst();
    }
}
