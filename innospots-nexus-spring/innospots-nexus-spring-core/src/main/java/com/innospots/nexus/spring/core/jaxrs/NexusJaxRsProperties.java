package com.innospots.nexus.spring.core.jaxrs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 宿主无关的 Jakarta REST 路由行为配置（与 console 权限、跨域设置无关）。
 *
 * @see NexusJaxRsConfiguration
 */
@ConfigurationProperties(prefix = "nexus.web.jersey")
public class NexusJaxRsProperties {

    /**
     * 未匹配 JAX-RS 资源时是否将请求继续交给后续 Servlet 链（含 Spring MVC）。
     *
     * <p>绑定 {@code nexus.web.jersey.forward-on-404}；默认 {@code true}：
     * 静态资源或未注册的 Spring MVC 端点仍可被后续处理器接管。
     * 设为 {@code false} 时由 Jersey 直接响应 404，避免落入 Spring Whitelabel HTML。</p>
     */
    private boolean forwardOn404 = true;

    public boolean isForwardOn404() {
        return forwardOn404;
    }

    public void setForwardOn404(boolean forwardOn404) {
        this.forwardOn404 = forwardOn404;
    }
}
