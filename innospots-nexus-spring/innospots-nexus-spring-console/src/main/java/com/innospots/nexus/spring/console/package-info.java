/**
 * Spring Boot 管理控制台应用模块。
 *
 * <p>依赖 {@code innospots-nexus-spring-core} 与 {@code innospots-nexus-console}；
 * 不含 {@code innospots-nexus-spring-service}（运行态服务框架，由引用方按需引入）。
 * 本模块不提供可执行 main；由 sample 或业务 application 模块组装启动。
 * 通过 {@link EnableNexusConsole} 引入控制台能力；可运行示例见
 * {@code innospots-nexus-sample}（{@code sample-spring-tenant} / {@code sample-spring-platform}）。</p>
 */
package com.innospots.nexus.spring.console;
