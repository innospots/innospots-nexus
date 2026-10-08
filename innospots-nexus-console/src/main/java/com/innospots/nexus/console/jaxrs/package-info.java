/**
 * 管理控制台 Jakarta REST 横切能力：请求过滤器、页面权限路径工具与会话绑定。
 *
 * <p>业务中立的异常映射与请求元数据支持位于
 * {@code com.innospots.nexus.core.jaxrs}；宿主无关的 Jersey 装配与
 * OpenAPI 文档页位于 {@code com.innospots.nexus.spring.core.jaxrs}；
 * console 专属过滤器装配见 {@code innospots-nexus-spring-console} 中的
 * {@code ConsoleJaxRsWebConfiguration}。</p>
 */
package com.innospots.nexus.console.jaxrs;
