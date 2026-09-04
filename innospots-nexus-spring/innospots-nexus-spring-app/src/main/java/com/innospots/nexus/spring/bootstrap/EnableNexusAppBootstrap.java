package com.innospots.nexus.spring.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.plugin.config.NexusAppPluginDaoConfiguration;

/**
 * 显式启用应用服务启动引导装配。
 *
 * <p>分层引入：</p>
 * <ul>
 *   <li>{@link NexusAppPersistenceConfiguration} — MyBatis-Plus 公共行为</li>
 *   <li>{@link NexusAppPluginDaoConfiguration} — Core 插件安装 DAO</li>
 *   <li>{@link NexusStartupConfiguration} — 宿主启动编排</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusAppPersistenceConfiguration.class,
        NexusAppPluginDaoConfiguration.class,
        NexusStartupConfiguration.class
})
public @interface EnableNexusAppBootstrap {
}
