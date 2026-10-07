/**
 * Platform 控制台内置 entry 插件：聚合租户、平台用户、注册策略等 {@code console@1} 贡献。
 *
 * <p>PageDsl 资源位于 classpath {@code ui-pages/platform/}；插件通过
 * {@code META-INF/services/com.innospots.nexus.core.plugin.contract.Plugin} 对外暴露。
 * 宿主应将 {@link com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins#REQUIRED_PLUGIN_IDS}
 * 列入 {@code nexus.plugins.required}。</p>
 *
 * <p>本包仅声明 UI 目录与菜单，不包含 REST 业务实现（见各 {@code *.endpoint} 包）。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.entry.PlatformConsoleEntryPlugin
 * @see com.innospots.nexus.console.entry.ConsoleModuleDescriptor
 */
package com.innospots.nexus.platform.entry;
