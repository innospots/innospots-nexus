package com.innospots.nexus.spring.console;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleAuthConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleCatalogConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleCredentialConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleDictionaryConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleLoggerConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleOpenApiConfiguration;
import com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration;
import com.innospots.nexus.spring.console.jaxrs.NexusScalarJerseyConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleMenuConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleNavigationConfiguration;
import com.innospots.nexus.spring.console.config.ConsolePermissionConfiguration;
import com.innospots.nexus.spring.console.config.ConsolePluginConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleRoleConfiguration;
import com.innospots.nexus.spring.core.bootstrap.EnableNexusHostBootstrap;
import com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost;

/**
 * 显式启用 Nexus 管理控制台完整 Spring 装配。
 *
 * <p>组合宿主引导、插件宿主，并按 console 业务域分别
 * {@link Import} 各 {@code Console*Configuration}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusHostBootstrap
@EnableNexusPluginHost
@Import({
        ConsoleAuthConfiguration.class,
        ConsolePluginConfiguration.class,
        ConsoleCatalogConfiguration.class,
        ConsoleCredentialConfiguration.class,
        ConsolePermissionConfiguration.class,
        ConsoleNavigationConfiguration.class,
        ConsoleMenuConfiguration.class,
        ConsoleRoleConfiguration.class,
        ConsoleDictionaryConfiguration.class,
        ConsoleLoggerConfiguration.class,
        ConsoleOpenApiConfiguration.class,
        NexusJaxRsConfiguration.class,
        NexusScalarJerseyConfiguration.class
})
public @interface EnableNexusConsole {
}
