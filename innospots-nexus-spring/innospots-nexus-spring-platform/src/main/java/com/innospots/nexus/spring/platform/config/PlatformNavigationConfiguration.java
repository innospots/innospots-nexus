package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.navigation.service.NavigationMenuComposer;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.platform.navigation.endpoint.PlatformNavigationMenuEndpoint;
import com.innospots.nexus.platform.navigation.service.PlatformNavigationService;

@Configuration
public class PlatformNavigationConfiguration {

    @Bean
    PlatformNavigationService platformNavigationService(
            NavigationMenuComposer navigationMenuComposer,
            AuthorizationSubjectResolver authorizationSubjectResolver) {
        return new PlatformNavigationService(navigationMenuComposer, authorizationSubjectResolver);
    }

    @Bean
    @Lazy
    PlatformNavigationMenuEndpoint platformNavigationMenuEndpoint(
            PlatformNavigationService platformNavigationService) {
        return new PlatformNavigationMenuEndpoint(platformNavigationService);
    }
}
