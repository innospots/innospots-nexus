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

    /** Jersey 请求侧安全（鉴权、页面权限路径规则）。 */
    private Security security = new Security();

    /** 浏览器跨域（CORS）响应头；默认关闭。 */
    private Cors cors = new Cors();

    /**
     * {@code nexus.console.web.security.*}：Jersey 鉴权相关路径与请求头。
     *
     * <p>{@link #enabled} 为总开关：关闭时 {@link com.innospots.nexus.spring.console.jaxrs.filter.ConsoleAuthenticationFilter}
     * 与 {@link com.innospots.nexus.spring.console.jaxrs.filter.ConsolePagePermissionFilter} 均不执行。
     * {@link #permitAllPatterns}、{@link #consolePathPatterns} 仅在 {@link #enabled} 为 {@code true} 时生效。</p>
     *
     * @author Smars
     * @date 2026/09/25
     */
    @Getter
    public static class Security {

        /**
         * 是否启用 Jersey 请求侧安全（Bearer 鉴权 + 控制台页面权限 Filter）；默认 {@code true}。
         *
         * <p>绑定 {@code nexus.console.web.security.enabled}。为 {@code false} 时不校验令牌、不调用
         * {@link com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer}；
         * 登录签发、令牌 TTL 等仍由 {@code nexus.console.auth.*} 控制。仅建议本地或受控集成环境使用。</p>
         */
        @Setter
        private boolean enabled = true;

        /**
         * 关闭 {@link #enabled} 时的开发用会话绑定；默认关闭。
         *
         * <p>仅当 {@link #enabled} 为 {@code false} 且 {@link DevSession#enabled} 为 {@code true} 时生效；
         * 生产环境请保持 {@link DevSession#enabled} 为 {@code false}，并配合 {@code spring.profiles.active=dev} 使用。</p>
         */
        @Setter
        private DevSession devSession = new DevSession();

        /**
         * 无需 Bearer 令牌的路径 Ant 模式（如 OpenAPI、登录、健康检查）。
         * 配置 {@code nexus.console.web.security.permit-all-patterns} 时整体替换默认列表。
         */
        private final List<String> permitAllPatterns = defaultPermitAll();

        /**
         * 控制台页面权限 Filter 生效的 HTTP 路径 Ant 模式：命中后除 Bearer 登录态外，还须通过
         * {@link com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer} 做页面级授权。
         *
         * <p>典型场景：管理端 UI 经 catalog 注册的代理路径（默认含 {@code /console/datasource/**}）
         * 拉取列表/图表数据；Filter 读取 {@link #pageKeyHeader}（当前页面在 catalog 中的 page key），
         * 结合 {@link com.innospots.nexus.base.thread.SessionContext} 中的 workspace、HTTP 方法与路径，
         * 判定当前用户是否拥有该页面对应资源的访问权。未命中本列表的路径不执行该授权逻辑。</p>
         *
         * <p>配置 {@code nexus.console.web.security.console-path-patterns} 时整体替换默认列表；
         * 置空列表表示不对任何路径启用页面权限 Filter。</p>
         */
        private final List<String> consolePathPatterns = new ArrayList<>(List.of("/console/datasource/**"));

        /**
         * 控制台页面权限鉴权用的页面键请求头名（默认 {@code X-Nexus-Page-Key}）。
         *
         * <p>仅当请求路径匹配 {@link #consolePathPatterns} 时必填；缺失或空白时返回 403。
         * 值须与控制台 catalog 中当前页面的 page key 一致，供
         * {@link com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer} 解析权限。</p>
         */
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
         * 设置控制台页面权限 Filter 生效的路径列表（见 {@link #consolePathPatterns} 字段说明）。
         *
         * @param consolePathPatterns 路径 Ant 模式；{@code null} 时清空列表（不启用页面权限 Filter）
         */
        public void setConsolePathPatterns(List<String> consolePathPatterns) {
            this.consolePathPatterns.clear();
            if (consolePathPatterns != null) {
                this.consolePathPatterns.addAll(consolePathPatterns);
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

        /**
         * {@code nexus.console.web.security.dev-session.*}：关闭请求侧安全时的固定会话快照。
         */
        @Getter
        @Setter
        public static class DevSession {

            /**
             * 是否注入 dev 会话；绑定 {@code nexus.console.web.security.dev-session.enabled}，默认 {@code false}。
             */
            private boolean enabled = false;

            /** 逻辑用户 ID（字符串，与令牌声明一致）。 */
            private String userId = "1";

            /** 租户 ID。 */
            private String tenantId = "dev-tenant";

            /** 工作区 ID。 */
            private String workspaceId = "dev-workspace";

            /** 可选项目 ID。 */
            private String projectId;

            /** 可选 {@link com.innospots.nexus.base.thread.TLC#SECURITY_REALM}。 */
            private String realm;

            /** 可选租户成员 ID（{@link com.innospots.nexus.base.thread.TLC#tenantMemberId}）。 */
            private String tenantMemberId;
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
