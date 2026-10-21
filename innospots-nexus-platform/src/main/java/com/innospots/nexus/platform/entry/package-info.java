/**
 * Platform 内置 console@2 entry：{@link com.innospots.nexus.platform.entry.PlatformConsoleEntryPlugin}
 * 经 {@link com.innospots.nexus.platform.entry.PlatformEntryPluginDescriptor} 与
 * {@link com.innospots.nexus.platform.entry.PlatformModuleEntrySupport} 组装。
 *
 * <p>PageDsl 位于 classpath {@code ui-pages/platform/}。宿主应将
 * {@link com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins#REQUIRED_PLUGIN_IDS}
 * 列入 {@code nexus.plugins.required}。</p>
 *
 * <p>贡献组装使用 plugin 模块 {@link com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions}；
 * 本包仅保留模块常量、插件 ID 与 SPI 实现。</p>
 */
package com.innospots.nexus.platform.entry;
