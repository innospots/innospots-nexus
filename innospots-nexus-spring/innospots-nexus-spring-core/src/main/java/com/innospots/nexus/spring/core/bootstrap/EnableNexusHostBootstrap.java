package com.innospots.nexus.spring.core.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.core.i18n.EnableNexusI18n;
import com.innospots.nexus.spring.core.jaxrs.EnableNexusJaxRs;
import com.innospots.nexus.spring.core.plugin.NexusPluginInstallationDaoConfiguration;
import com.innospots.nexus.spring.core.setting.NexusSystemSettingConfiguration;

/**
 * 显式启用 Nexus 宿主公共启动引导（持久化、插件安装 DAO、启动编排、i18n、通用 JAX-RS 装配）。
 *
 * <p>由 API 应用与管理控制台 runnable 通过各自 {@code @Enable*} 组合注解复用。
 * 插件运行时 Bean 须额外标注 {@link com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusI18n
@EnableNexusJaxRs
@Import({
        NexusPersistenceConfiguration.class,
        NexusTransactionConfiguration.class,
        NexusSystemSettingConfiguration.class,
        NexusPluginInstallationDaoConfiguration.class,
        NexusStartupConfiguration.class
})
public @interface EnableNexusHostBootstrap {
}
