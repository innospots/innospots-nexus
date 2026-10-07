/**
 * Spring Boot 管理控制台应用模块。
 *
 * <p>依赖 {@code innospots-nexus-spring-core} 与 {@code innospots-nexus-console}；
 * 不含 {@code innospots-nexus-spring-service}（运行态服务框架，由引用方按需引入）。
 * 本模块不提供可执行 main，也不携带 {@code application.yaml}；由可执行服务模块配置
 * {@code nexus.console.*}（绑定 {@link com.innospots.nexus.spring.console.config.ConsoleAuthProperties}）。
 * 通过 {@link EnableNexusConsole} 引入控制台装配；示例见
 * {@code innospots-nexus-sample-spring-portal} / {@code innospots-nexus-sample-spring-platform}。</p>
 */
package com.innospots.nexus.spring.console;
