/**
 * 当前 JVM 内的插件运行时宿主入口。
 * {@link com.innospots.nexus.core.plugin.runtime.PluginManager} 是运行态唯一事实源；
 * 实例由宿主创建，不提供全局单例，生命周期调用在管理器内部串行化。
 */
package com.innospots.nexus.core.plugin.runtime;
