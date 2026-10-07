package com.innospots.nexus.spring.service.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.websocket.config.ServiceWebSocketModuleConfiguration;

/**
 * 启用 Nexus WebSocket 运行时：Registry、配置与 {@code WebSocketService}。
 *
 * <p>仍须 {@link EnableNexusServiceHttp}（握手经 HTTP Filter 建立上下文），并自行注册
 * {@code SpringWebSocketEndpointBridge} / 路径映射（见模块文档）。</p>
 *
 * @author Smars
 * @date 2026/09/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServiceWebSocketModuleConfiguration.class)
public @interface EnableNexusServiceWebSocket {
}
