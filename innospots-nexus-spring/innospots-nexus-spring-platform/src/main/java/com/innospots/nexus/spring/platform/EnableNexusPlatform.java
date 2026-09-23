package com.innospots.nexus.spring.platform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.EnableNexusConsole;
import com.innospots.nexus.spring.platform.config.PlatformAuthConfiguration;

/**
 * 显式启用 Nexus 管理控制台与 platform 运维域 Spring 装配。
 *
 * <p>组合 {@link EnableNexusConsole} 与 platform 域 {@code Platform*Configuration}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see EnableNexusConsole
 * @see PlatformAuthConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusConsole
@Import(PlatformAuthConfiguration.class)
public @interface EnableNexusPlatform {
}
