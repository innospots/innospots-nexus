package com.innospots.nexus.spring.core.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 显式启用 Nexus 插件宿主 Spring 装配。
 *
 * <p>在应用主配置类上标注，引入插件宿主运行时、启动任务与安装表 DAO 扫描。
 * 须配合 {@link com.innospots.nexus.spring.core.bootstrap.EnableNexusSimpleBootstrap}
 * 或 {@link com.innospots.nexus.spring.core.bootstrap.EnableNexusHostBootstrap} 提供数据源与
 * MyBatis 运行态；亦可自行注册等价
 * {@link com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao} Bean。</p>
 *
 * <p>管理控制台请使用 {@link com.innospots.nexus.spring.console.EnableNexusConsole}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusPluginInstallationDaoConfiguration.class,
        PluginHostConfiguration.class,
        PluginHostStartupConfiguration.class
})
public @interface EnableNexusPluginHost {
}

