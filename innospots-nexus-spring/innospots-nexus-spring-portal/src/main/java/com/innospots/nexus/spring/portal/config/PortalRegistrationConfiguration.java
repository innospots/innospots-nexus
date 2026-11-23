package com.innospots.nexus.spring.portal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.user.portal.service.PortalUserService;
import com.innospots.nexus.portal.registration.endpoint.TenantRegistrationEndpoint;
import com.innospots.nexus.portal.registration.service.TenantRegistrationFacade;

/**
 * portal 租户域注册的 Spring 装配。
 *
 * <p>注册与认证分离：本配置只装配身份注册端点与编排，不依赖认证门面；
 * 登录、令牌与验证码见 {@link PortalAuthConfiguration}。</p>
 *
 * @author Smars
 * @date 2026/11/22
 * @see TenantRegistrationEndpoint
 */
@Configuration
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PortalRegistrationConfiguration {

    /**
     * 租户域身份注册编排。
     *
     * @param portalUserService 平台注册用户服务
     * @return 注册门面
     */
    @Bean
    TenantRegistrationFacade tenantRegistrationFacade(PortalUserService portalUserService) {
        return new TenantRegistrationFacade(portalUserService);
    }

    /**
     * 租户域注册 REST 资源。
     *
     * @param tenantRegistrationFacade 注册门面
     * @return 注册端点
     */
    @Bean
    @Lazy
    TenantRegistrationEndpoint tenantRegistrationEndpoint(
            TenantRegistrationFacade tenantRegistrationFacade) {
        return new TenantRegistrationEndpoint(tenantRegistrationFacade);
    }
}
