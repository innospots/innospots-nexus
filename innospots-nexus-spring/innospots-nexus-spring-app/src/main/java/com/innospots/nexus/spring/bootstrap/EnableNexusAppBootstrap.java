package com.innospots.nexus.spring.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.spring.plugin.config.EnableNexusPluginHost;
import org.springframework.context.annotation.Import;

/**
 * 显式启用应用服务启动引导装配。
 *
 * <p>引入内存 H2 数据源、插件安装表 DDL，并通过 {@link org.mybatis.spring.annotation.MapperScan}
 * 注册 Core 模块中的 {@link com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao}。
 * 与 {@link EnableNexusPluginHost} 配合使用。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({NexusAppBootstrapConfiguration.class, NexusStartupConfiguration.class})
public @interface EnableNexusAppBootstrap {
}
