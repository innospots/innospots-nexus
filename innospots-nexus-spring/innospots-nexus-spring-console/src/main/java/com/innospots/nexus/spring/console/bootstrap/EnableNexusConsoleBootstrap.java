package com.innospots.nexus.spring.console.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.bootstrap.NexusAppBootstrapConfiguration;
import com.innospots.nexus.spring.bootstrap.NexusStartupConfiguration;

/**
 * 显式启用管理控制台启动引导装配。
 *
 * <p>复用应用服务引导，并引入控制台专属引导配置。
 * 与 {@link com.innospots.nexus.spring.console.EnableNexusConsole} 配合使用。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusAppBootstrapConfiguration.class,
        NexusStartupConfiguration.class,
        NexusConsoleBootstrapConfiguration.class,
        ConsoleCatalogConfiguration.class
})
public @interface EnableNexusConsoleBootstrap {
}
