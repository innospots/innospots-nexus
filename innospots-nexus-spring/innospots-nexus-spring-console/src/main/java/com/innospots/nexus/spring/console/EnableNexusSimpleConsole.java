package com.innospots.nexus.spring.console;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleAuthConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleCredentialConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleJaxRsWebConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleLoggerConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleNavigationConfiguration;
import com.innospots.nexus.spring.console.config.ConsolePermissionConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleRoleConfiguration;
import com.innospots.nexus.spring.core.bootstrap.EnableNexusHostBootstrap;

/**
 * 轻量管理控制台 Spring 装配：认证、凭证、权限、角色、导航与 JAX-RS Web，不含插件宿主、
 * 目录索引、菜单与字典域。
 *
 * <p>需要完整控制台能力时请使用 {@link EnableNexusConsole}。</p>
 *
 * @see EnableNexusConsole
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusHostBootstrap
@Import({
        ConsoleAuthConfiguration.class,
        ConsoleCredentialConfiguration.class,
        ConsolePermissionConfiguration.class,
        ConsoleNavigationConfiguration.class,
        ConsoleRoleConfiguration.class,
        ConsoleLoggerConfiguration.class,
        ConsoleJaxRsWebConfiguration.class
})
public @interface EnableNexusSimpleConsole {
}
