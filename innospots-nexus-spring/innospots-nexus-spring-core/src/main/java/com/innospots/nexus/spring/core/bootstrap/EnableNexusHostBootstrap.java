package com.innospots.nexus.spring.core.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.core.i18n.NexusI18nConfiguration;
import com.innospots.nexus.spring.core.i18n.NexusI18nWebConfiguration;
import com.innospots.nexus.spring.core.plugin.NexusPluginInstallationDaoConfiguration;

/**
 * 显式启用 Nexus 宿主公共启动引导（持久化、插件安装 DAO、启动编排、i18n）。
 *
 * <p>由 API 应用与管理控制台 runnable 通过各自 {@code @Enable*} 组合注解复用。
 * 插件运行时 Bean 须额外标注 {@link com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusPersistenceConfiguration.class,
        NexusPluginInstallationDaoConfiguration.class,
        NexusStartupConfiguration.class,
        NexusI18nConfiguration.class,
        NexusI18nWebConfiguration.class
})
public @interface EnableNexusHostBootstrap {
}
