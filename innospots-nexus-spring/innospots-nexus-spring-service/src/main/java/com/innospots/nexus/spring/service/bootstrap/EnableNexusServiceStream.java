package com.innospots.nexus.spring.service.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.stream.config.ServiceStreamModuleConfiguration;

/**
 * 启用 Nexus 流式响应：{@code StreamManager} 与 {@code StreamSession} SSE 写回（MVC / WebFlux 按条件生效）。
 *
 * <p>HTTP 入口仍须 {@link EnableNexusServiceHttp}，以便 Filter 建立 {@code ServiceContext} 与取消令牌。</p>
 *
 * @author Smars
 * @date 2026/09/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        ServiceStreamModuleConfiguration.ServletStreamModuleConfiguration.class,
        ServiceStreamModuleConfiguration.ReactiveStreamModuleConfiguration.class
})
public @interface EnableNexusServiceStream {
}
