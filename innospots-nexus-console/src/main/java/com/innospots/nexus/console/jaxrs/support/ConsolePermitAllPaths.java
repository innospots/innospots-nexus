package com.innospots.nexus.console.jaxrs.support;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings#getPermitAllPatterns()} 默认 Ant 模式。
 *
 * <p>命中路径在鉴权 Filter 中与 OpenAPI 一样<strong>完全跳过</strong> Bearer 校验（含公共开放 API）。</p>
 */
public final class ConsolePermitAllPaths {

    /** OpenAPI 规范与文档 UI（{@link com.innospots.nexus.console.openapi.NexusConsoleOpenApiDefinition}）。 */
    public static final String OPENAPI = "/openapi/**";

    /** 控制台遗留公共 API 前缀（{@link ConsolePublicApiPaths#LEGACY_PUBLIC_PATTERN}）。 */
    public static final String PUBLIC_API_LEGACY = ConsolePublicApiPaths.LEGACY_PUBLIC_PATTERN;

    /** 各业务域公共 API（{@link ConsolePublicApiPaths#DOMAIN_PUBLIC_PATTERN}）。 */
    public static final String PUBLIC_API_DOMAIN = ConsolePublicApiPaths.DOMAIN_PUBLIC_PATTERN;

    /** 应用健康检查。 */
    public static final String HEALTH = "/health";

    /** Spring Boot Actuator 健康端点。 */
    public static final String ACTUATOR_HEALTH = "/actuator/health/**";

    /** 站点根路径（{@link com.innospots.nexus.console.endpoint.MainRootEndpoint} 重定向）。 */
    public static final String ROOT = "/";

    private static final List<String> DEFAULT_PATTERNS = List.of(
            ROOT,
            OPENAPI,
            PUBLIC_API_LEGACY,
            PUBLIC_API_DOMAIN,
            HEALTH,
            ACTUATOR_HEALTH);

    private ConsolePermitAllPaths() {
    }

    /**
     * 返回默认免登录路径模式（可变副本，供 {@link com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings} 初始化或重置）。
     */
    public static List<String> defaultPatterns() {
        return new ArrayList<>(DEFAULT_PATTERNS);
    }
}
