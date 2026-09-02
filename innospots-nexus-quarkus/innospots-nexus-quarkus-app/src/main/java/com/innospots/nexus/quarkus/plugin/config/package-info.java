/**
 * 插件宿主 Quarkus CDI 装配。
 *
 * <p>在 {@link io.quarkus.runtime.StartupEvent} 后启用插件子系统，在
 * {@link io.quarkus.runtime.ShutdownEvent} 时释放运行时。Console 专属 Bean 由
 * {@code innospots-nexus-quarkus-console} 模块补充。</p>
 */
package com.innospots.nexus.quarkus.plugin.config;
