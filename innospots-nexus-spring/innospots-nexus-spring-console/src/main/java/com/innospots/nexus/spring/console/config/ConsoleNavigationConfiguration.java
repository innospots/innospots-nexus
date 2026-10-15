package com.innospots.nexus.spring.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;

/**
 * {@code console.navigation} 域 Spring 装配：导航菜单组装（REST 由 platform 实现）。
 */
@Configuration
public class ConsoleNavigationConfiguration {

    @Bean
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }
}
