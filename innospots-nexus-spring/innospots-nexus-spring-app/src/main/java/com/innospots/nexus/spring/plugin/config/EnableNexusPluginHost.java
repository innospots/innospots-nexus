package com.innospots.nexus.spring.plugin.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 显式启用 Nexus 插件宿主 Spring 装配。
 *
 * <p>在应用主配置类上标注，引入 {@link PluginHostConfiguration}。
 * 须配合 {@link com.innospots.nexus.spring.bootstrap.EnableNexusAppBootstrap} 提供
 * {@link com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao} 等基础设施，
 * 或自行注册等价 Bean。</p>
 *
 * <p>管理控制台请使用 {@link com.innospots.nexus.spring.console.EnableNexusConsole}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PluginHostConfiguration.class)
public @interface EnableNexusPluginHost {
}
