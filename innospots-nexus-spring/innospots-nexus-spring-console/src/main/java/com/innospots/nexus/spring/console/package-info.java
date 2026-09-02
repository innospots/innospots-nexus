/**
 * Spring Boot 管理控制台应用模块。
 *
 * <p>依赖 {@code innospots-nexus-spring-app}，并引入 {@code console} 与 {@code kernel}。
 * 通过 {@link EnableNexusConsole} 与 {@link com.innospots.nexus.spring.console.bootstrap.EnableNexusConsoleBootstrap}
 * 显式引入；进程入口见 {@link com.innospots.nexus.spring.console.server.NexusConsoleServer}。</p>
 */
package com.innospots.nexus.spring.console;
