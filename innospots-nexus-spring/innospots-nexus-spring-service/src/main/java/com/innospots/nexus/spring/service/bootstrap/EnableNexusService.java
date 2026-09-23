package com.innospots.nexus.spring.service.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.http.config.ServiceReactiveConfiguration;
import com.innospots.nexus.spring.service.http.config.ServiceServletConfiguration;
import com.innospots.nexus.spring.service.stream.config.ServiceStreamModuleConfiguration;
import com.innospots.nexus.spring.service.transfer.config.ServiceTransferModuleConfiguration;
import com.innospots.nexus.spring.service.websocket.config.ServiceWebSocketModuleConfiguration;

/**
 * 启用 Nexus 服务框架全部 Spring 装配（HTTP + Stream + Transfer + WebSocket）。
 *
 * <p>等价于同时标注 {@link EnableNexusServiceHttp}、{@link EnableNexusServiceStream}、
 * {@link EnableNexusServiceTransfer}、{@link EnableNexusServiceWebSocket}。</p>
 *
 * @author Smars
 * @date 2026/09/16
 * @see EnableNexusServiceHttp
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        ServiceServletConfiguration.class,
        ServiceReactiveConfiguration.class,
        ServiceStreamModuleConfiguration.ServletStreamModuleConfiguration.class,
        ServiceStreamModuleConfiguration.ReactiveStreamModuleConfiguration.class,
        ServiceTransferModuleConfiguration.ServletTransferModuleConfiguration.class,
        ServiceTransferModuleConfiguration.ReactiveTransferModuleConfiguration.class,
        ServiceWebSocketModuleConfiguration.class
})
public @interface EnableNexusService {
}
