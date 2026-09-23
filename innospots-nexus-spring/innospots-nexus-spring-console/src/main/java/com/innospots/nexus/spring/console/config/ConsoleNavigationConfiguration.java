package com.innospots.nexus.spring.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;

/**
 * {@code console.navigation} 域 Spring 装配。
 */
@Configuration
public class ConsoleNavigationConfiguration {

    @Bean
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }

    @Bean
    @Lazy
    NavigationMenuEndpoint navigationMenuEndpoint(
            NavigationMenuAssembler assembler,
            AuthorizationSubjectResolver subjectResolver) {
        return new NavigationMenuEndpoint(assembler, subjectResolver);
    }
}
