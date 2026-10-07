/**
 * Nexus 服务框架 Spring adapter：按能力域分包。
 *
 * <ul>
 *   <li>{@link com.innospots.nexus.spring.service.bootstrap} — 应用显式启用的 {@code @EnableNexusService*} 注解</li>
 *   <li>{@link com.innospots.nexus.spring.service.core} — 共享运行时与 {@code service.*} 配置属性</li>
 *   <li>{@link com.innospots.nexus.spring.service.http} — HTTP API（Filter、错误映射、Invocation 桥）</li>
 *   <li>{@link com.innospots.nexus.spring.service.stream} — 流式 SSE / {@code StreamSession}</li>
 *   <li>{@link com.innospots.nexus.spring.service.transfer} — {@code DownloadResource} 写回</li>
 *   <li>{@link com.innospots.nexus.spring.service.websocket} — WebSocket Registry 与 Spring Bridge</li>
 * </ul>
 */
package com.innospots.nexus.spring.service;
