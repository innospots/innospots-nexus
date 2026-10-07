/**
 * Quarkus 管理控制台应用模块。
 *
 * <p>依赖 {@code innospots-nexus-quarkus-core}，并引入 {@code console} 中立库。
 * portal / platform 域装配分别由 {@code innospots-nexus-quarkus-portal} 与
 * {@code innospots-nexus-quarkus-platform} 提供。
 * 补充插件 Contribution、管理 REST，并承载用户、权限、菜单等控制台域装配（预留包）。
 * 进程入口见 {@link com.innospots.nexus.quarkus.console.server.NexusConsoleServer}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.quarkus.console;

