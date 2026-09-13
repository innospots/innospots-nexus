/**
 * 插件启动周期内的资源所有权与逆序关闭。
 * 资源在单次 start/stop 周期内注册，关闭顺序与注册顺序相反，且每个资源只关闭一次。
 */
package com.innospots.nexus.core.plugin.resource;
