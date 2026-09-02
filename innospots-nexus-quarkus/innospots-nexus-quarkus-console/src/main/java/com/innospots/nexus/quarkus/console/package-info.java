/**
 * Quarkus 管理控制台应用模块。
 *
 * <p>依赖 {@code innospots-nexus-quarkus-app}，并引入 {@code console} 与 {@code kernel}。
 * 补充插件 Contribution、管理 REST，并承载用户、权限、菜单等控制台域装配（预留包）。
 * 进程入口见 {@link com.innospots.nexus.quarkus.console.server.NexusConsoleServer}。</p>
 */
package com.innospots.nexus.quarkus.console;
