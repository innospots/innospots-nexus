package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.platform.permission.endpoint.PlatformCurrentAuthorizationEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformOrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformPageAccessEndpoint;
import com.innospots.nexus.platform.permission.service.PlatformActionAccessService;
import com.innospots.nexus.platform.permission.service.PlatformPageAccessService;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;

@Configuration
public class PlatformPermissionConfiguration {

    @Bean
    PlatformActionAccessService platformActionAccessService(EndpointGrantManager manager,
            ConsoleResourcePermissionAuthorizer authorizer, AuthorizationSubjectResolver subjects,
            PermissionVisibilityService visibility, ConsoleCatalogService catalog) {
        return new PlatformActionAccessService(manager, authorizer, subjects, visibility, catalog);
    }

    @Bean
    PlatformPermissionService platformPermissionService(
            PermissionGrantService permissionGrantService,
            AuthorizationSubjectResolver authorizationSubjectResolver,
            PermissionVisibilityService permissionVisibilityService) {
        return new PlatformPermissionService(
                permissionGrantService,
                authorizationSubjectResolver,
                permissionVisibilityService);
    }

    @Bean
    @Lazy
    PlatformGrantManagementEndpoint platformGrantManagementEndpoint(
            PlatformPermissionService platformPermissionService) {
        return new PlatformGrantManagementEndpoint(platformPermissionService);
    }

    @Bean
    @Lazy
    PlatformOrganizationUnitGrantManagementEndpoint platformOrganizationUnitGrantManagementEndpoint(
            PlatformPermissionService platformPermissionService) {
        return new PlatformOrganizationUnitGrantManagementEndpoint(platformPermissionService);
    }

    @Bean
    @Lazy
    PlatformCurrentAuthorizationEndpoint platformCurrentAuthorizationEndpoint(
            PlatformPermissionService platformPermissionService) {
        return new PlatformCurrentAuthorizationEndpoint(platformPermissionService);
    }

    @Bean
    PlatformPageAccessService platformPageAccessService(
            PageAccessVerifier pageAccessVerifier,
            AuthorizationSubjectResolver authorizationSubjectResolver,
            PlatformActionAccessService actionAccessService,
            ConsoleWebProperties consoleWebProperties) {
        return new PlatformPageAccessService(pageAccessVerifier, authorizationSubjectResolver,
                actionAccessService, consoleWebProperties.getSecurity());
    }

    @Bean
    @Lazy
    PlatformPageAccessEndpoint platformPageAccessEndpoint(PlatformPageAccessService platformPageAccessService) {
        return new PlatformPageAccessEndpoint(platformPageAccessService);
    }
}
