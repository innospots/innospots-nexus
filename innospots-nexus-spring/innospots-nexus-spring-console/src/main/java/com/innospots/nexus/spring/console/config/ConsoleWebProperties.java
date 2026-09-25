package com.innospots.nexus.spring.console.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 管理控制台 Jersey Web 横切行为配置。
 *
 * <p>绑定 {@code nexus.console.web.*}，由 {@link ConsoleJaxRsWebConfiguration} 注册过滤器与异常映射。</p>
 *
 * @author Smars
 * @date 2026/09/25
 * @see ConsoleJaxRsWebConfiguration
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus.console.web")
public class ConsoleWebProperties {

    /** 是否启用控制台 Jersey 过滤器、CORS 与统一异常映射；默认 {@code true}。 */
    private boolean enabled = true;

    /** 鉴权与数据源代理路径规则。 */
    private Security security = new Security();

    /** 浏览器跨域（CORS）响应头；默认关闭。 */
    private Cors cors = new Cors();

    /**
     * {@code nexus.console.web.security.*}：访问控制与 catalog datasource 代理鉴权。
     *
     * @author Smars
     * @date 2026/09/25
     */
    @Getter
    public static class Security {

        /**
         * 无需 Bearer 令牌的路径 Ant 模式（如 OpenAPI、登录、健康检查）。
         * 配置 {@code nexus.console.web.security.permit-all-patterns} 时整体替换默认列表。
         */
        private final List<String> permitAllPatterns = defaultPermitAll();

        /**
         * 需经 {@link com.innospots.nexus.console.permission.authorization.RequestAuthorizer}
         * 鉴权的 datasource 代理路径 Ant 模式。
         */
        private final List<String> datasourcePathPatterns = new ArrayList<>(List.of("/console/datasource/**"));

        /** 页面资源键请求头名，用于 datasource 鉴权时关联 catalog 权限。 */
        @Setter
        private String pageKeyHeader = "X-Nexus-Page-Key";

        /**
         * 设置免登录路径列表。
         *
         * @param permitAllPatterns 路径模式；{@code null} 时仅清空自定义项并保留构造期默认
         */
        public void setPermitAllPatterns(List<String> permitAllPatterns) {
            this.permitAllPatterns.clear();
            if (permitAllPatterns != null) {
                this.permitAllPatterns.addAll(permitAllPatterns);
            }
        }

        /**
         * 设置 datasource 鉴权路径列表。
         *
         * @param datasourcePathPatterns 路径模式；{@code null} 时清空列表
         */
        public void setDatasourcePathPatterns(List<String> datasourcePathPatterns) {
            this.datasourcePathPatterns.clear();
            if (datasourcePathPatterns != null) {
                this.datasourcePathPatterns.addAll(datasourcePathPatterns);
            }
        }

        private static List<String> defaultPermitAll() {
            List<String> patterns = new ArrayList<>();
            patterns.add("/openapi/**");
            patterns.add("/auth/**");
            patterns.add("/tenant/auth/**");
            patterns.add("/platform/auth/**");
            patterns.add("/health");
            patterns.add("/actuator/health/**");
            return patterns;
        }
    }

    /**
     * {@code nexus.console.web.cors.*}：Jersey {@code ContainerResponseFilter} 输出的 CORS 头。
     *
     * @author Smars
     * @date 2026/09/25
     */
    @Getter
    @Setter
    public static class Cors {

        /** 是否向响应附加 CORS 头；默认 {@code false}。 */
        private boolean enabled = false;

        /** {@code Access-Control-Allow-Origin}；默认 {@code *}。 */
        private List<String> allowedOrigins = List.of("*");

        /** {@code Access-Control-Allow-Methods}。 */
        private List<String> allowedMethods = List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");

        /** {@code Access-Control-Allow-Headers}。 */
        private List<String> allowedHeaders = List.of("*");

        /** {@code Access-Control-Expose-Headers}。 */
        private List<String> exposedHeaders = List.of();

        /** {@code Access-Control-Allow-Credentials}。 */
        private boolean allowCredentials = false;

        /** 预检请求 {@code Access-Control-Max-Age}（秒）。 */
        private long maxAgeSeconds = 3600L;

        /**
         * 设置允许的来源列表。
         *
         * @param allowedOrigins 来源；{@code null} 时置为空列表
         */
        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }

        /**
         * 设置允许的 HTTP 方法列表。
         *
         * @param allowedMethods 方法名；{@code null} 时置为空列表
         */
        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods == null ? List.of() : List.copyOf(allowedMethods);
        }

        /**
         * 设置允许的请求头列表。
         *
         * @param allowedHeaders 头名；{@code null} 时置为空列表
         */
        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders == null ? List.of() : List.copyOf(allowedHeaders);
        }

        /**
         * 设置暴露给浏览器的响应头列表。
         *
         * @param exposedHeaders 头名；{@code null} 时置为空列表
         */
        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders == null ? List.of() : List.copyOf(exposedHeaders);
        }
    }
}
