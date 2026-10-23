package com.innospots.nexus.core.endpoint.contract;

/**
 * 控制台与公共开放 REST API 根路径前缀（JAX-RS 契约共享常量）。
 */
public final class ConsoleRestPaths {

    /** 控制台管理 REST API 根路径前缀。 */
    public static final String API_PREFIX = "/api/d/nexus";

    /**
     * 公共开放 REST API 根路径前缀（无需鉴权）。
     */
    public static final String PUBLIC_API_PREFIX = "/api/public";

    private ConsoleRestPaths() {
    }
}
