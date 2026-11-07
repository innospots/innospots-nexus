package com.innospots.nexus.spring.portal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.EnableNexusConsole;
import com.innospots.nexus.spring.portal.config.PortalAuthConfiguration;
import com.innospots.nexus.spring.portal.config.PortalScopeConfiguration;
import com.innospots.nexus.spring.portal.config.PortalUserConfiguration;

/**
 * 显式启用 Nexus 管理控制台与 portal 租户域 Spring 装配。
 *
 * <p>组合 {@link EnableNexusConsole} 与 portal 域各领域配置：</p>
 * <ul>
 *   <li>{@link PortalAuthConfiguration}：登录、令牌与认证端点；</li>
 *   <li>{@link PortalUserConfiguration}：平台注册用户、密码与 OAuth 身份；</li>
 *   <li>{@link PortalScopeConfiguration}：租户 → 工作区 → 项目作用域。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/23
 * @see EnableNexusConsole
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusConsole
@Import({
        PortalAuthConfiguration.class,
        PortalUserConfiguration.class,
        PortalScopeConfiguration.class
})
public @interface EnableNexusPortal {
}
