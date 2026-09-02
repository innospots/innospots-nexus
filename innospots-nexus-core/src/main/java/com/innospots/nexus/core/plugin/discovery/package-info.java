/**
 * classpath 插件发现与定义编译入口。
 * 发现产物为不可跨管理器共享的 {@link com.innospots.nexus.core.plugin.discovery.PluginCatalog}；
 * 单插件 YAML 失败进入拒绝报告，全局身份冲突仍视为致命错误。
 */
package com.innospots.nexus.core.plugin.discovery;
