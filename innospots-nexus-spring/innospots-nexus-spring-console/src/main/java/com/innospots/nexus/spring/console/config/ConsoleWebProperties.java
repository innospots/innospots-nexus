package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.console.jaxrs.web.ConsoleWebCorsSettings;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebJerseySettings;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;

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
    private ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();

    /** 浏览器跨域（CORS）响应头；默认关闭。 */
    private ConsoleWebCorsSettings cors = new ConsoleWebCorsSettings();

    /** Jersey Filter 路由（如 404 是否透传 Spring MVC）；默认不向 MVC 转发。 */
    private ConsoleWebJerseySettings jersey = new ConsoleWebJerseySettings();
}
