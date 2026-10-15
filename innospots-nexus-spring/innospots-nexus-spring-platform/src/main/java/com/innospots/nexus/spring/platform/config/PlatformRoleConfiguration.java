package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleBindingEndpoint;
import com.innospots.nexus.platform.role.endpoint.PlatformRoleEndpoint;
import com.innospots.nexus.platform.role.service.PlatformRoleBindingService;
import com.innospots.nexus.platform.role.service.PlatformRoleService;

/**
 * 运营平台角色 Spring 装配：生命周期与绑定 REST。
 */
@Configuration
public class PlatformRoleConfiguration {

    @Bean
    PlatformRoleService platformRoleService(RoleOperator roleOperator) {
        return new PlatformRoleService(roleOperator);
    }

    @Bean
    PlatformRoleBindingService platformRoleBindingService(RoleBindingOperator roleBindingOperator) {
        return new PlatformRoleBindingService(roleBindingOperator);
    }

    @Bean
    @Lazy
    PlatformRoleEndpoint platformRoleEndpoint(PlatformRoleService platformRoleService) {
        return new PlatformRoleEndpoint(platformRoleService);
    }

    @Bean
    @Lazy
    PlatformRoleBindingEndpoint platformRoleBindingEndpoint(
            PlatformRoleBindingService platformRoleBindingService) {
        return new PlatformRoleBindingEndpoint(platformRoleBindingService);
    }
}
