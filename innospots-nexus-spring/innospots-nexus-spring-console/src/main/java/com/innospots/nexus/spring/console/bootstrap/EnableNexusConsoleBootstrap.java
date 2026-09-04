package com.innospots.nexus.spring.console.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.bootstrap.NexusAppPersistenceConfiguration;
import com.innospots.nexus.spring.bootstrap.NexusStartupConfiguration;
import com.innospots.nexus.spring.catalog.config.ConsoleCatalogConfiguration;
import com.innospots.nexus.spring.plugin.config.NexusAppPluginDaoConfiguration;

/**
 * 显式启用管理控制台启动引导装配。
 *
 * <p>分层引入：</p>
 * <ul>
 *   <li>应用层：{@link NexusAppPersistenceConfiguration}、{@link NexusAppPluginDaoConfiguration}、
 *       {@link NexusStartupConfiguration}</li>
 *   <li>控制台层：{@link NexusConsolePersistenceConfiguration}（各域 DAO）、
 *       {@link ConsoleCatalogConfiguration}（目录/导航服务）</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusAppPersistenceConfiguration.class,
        NexusAppPluginDaoConfiguration.class,
        NexusStartupConfiguration.class,
        NexusConsolePersistenceConfiguration.class,
        ConsoleCatalogConfiguration.class
})
public @interface EnableNexusConsoleBootstrap {
}
