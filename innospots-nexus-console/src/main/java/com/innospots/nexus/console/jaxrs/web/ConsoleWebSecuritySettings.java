package com.innospots.nexus.console.jaxrs.web;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.jaxrs.support.ConsolePermitAllPaths;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

import lombok.Getter;
import lombok.Setter;

/**
 * Jersey 鉴权相关路径与请求头配置。
 */
@Getter
public class ConsoleWebSecuritySettings {

    /**
     * 是否启用 Jersey 请求侧安全（Bearer 鉴权 + 控制台页面权限 Filter）；默认 {@code true}。
     */
    @Setter
    private boolean enabled = true;

    /** 关闭 {@link #enabled} 时的开发用会话绑定；默认关闭。 */
    @Setter
    private ConsoleWebDevSessionSettings devSession = new ConsoleWebDevSessionSettings();

    /**
     * 无需 Bearer 令牌的路径 Ant 模式（如 OpenAPI、登录、健康检查）。
     */
    private final List<String> permitAllPatterns = ConsolePermitAllPaths.defaultPatterns();

    /**
     * 控制台页面权限 Filter 生效的 HTTP 路径 Ant 模式。
     *
     * @see ConsolePagePermissionAuthorizer
     */
    private final List<String> consolePathPatterns = new ArrayList<>(List.of("/console/datasource/**"));

    /** 控制台页面权限鉴权用的页面键请求头名（默认 {@code X-Nexus-Page-Key}）。 */
    @Setter
    private String pageKeyHeader = "X-Nexus-Page-Key";

    /**
     * 设置免登录路径列表。
     *
     * @param permitAllPatterns 路径模式；{@code null} 时仅清空自定义项并保留构造期默认
     */
    public void setPermitAllPatterns(List<String> permitAllPatterns) {
        this.permitAllPatterns.clear();
        if (permitAllPatterns == null) {
            this.permitAllPatterns.addAll(ConsolePermitAllPaths.defaultPatterns());
            return;
        }
        this.permitAllPatterns.addAll(permitAllPatterns);
    }

    /**
     * 设置控制台页面权限 Filter 生效的路径列表。
     *
     * @param consolePathPatterns 路径 Ant 模式；{@code null} 时清空列表（不启用页面权限 Filter）
     */
    public void setConsolePathPatterns(List<String> consolePathPatterns) {
        this.consolePathPatterns.clear();
        if (consolePathPatterns != null) {
            this.consolePathPatterns.addAll(consolePathPatterns);
        }
    }

}
