package com.innospots.nexus.spring.portal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.EnableNexusConsole;
import com.innospots.nexus.spring.portal.config.PortalAuthConfiguration;

/**
 * 显式启用 Nexus 管理控制台与 portal 租户域 Spring 装配。
 *
 * <p>组合 {@link EnableNexusConsole} 与 portal 域 {@code Portal*Configuration}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see EnableNexusConsole
 * @see PortalAuthConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusConsole
@Import(PortalAuthConfiguration.class)
public @interface EnableNexusPortal {
}
