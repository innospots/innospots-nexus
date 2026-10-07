package com.innospots.nexus.spring.service.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.http.config.ServiceReactiveConfiguration;
import com.innospots.nexus.spring.service.http.config.ServiceServletConfiguration;

/**
 * 启用 Nexus HTTP API 装配：中立运行时、观测/治理、Servlet MVC 或 WebFlux 过滤器与错误映射。
 *
 * <p>流式、下载、WebSocket 须另行 {@link EnableNexusServiceStream}、
 * {@link EnableNexusServiceTransfer}、{@link EnableNexusServiceWebSocket}。</p>
 *
 * @author Smars
 * @date 2026/09/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        ServiceServletConfiguration.class,
        ServiceReactiveConfiguration.class
})
public @interface EnableNexusServiceHttp {
}
