package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.platform.permission.endpoint.PlatformCurrentAuthorizationEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformOrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.permission.endpoint.PlatformPageAccessEndpoint;
import com.innospots.nexus.platform.permission.service.PlatformPageAccessService;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Configuration
public class PlatformPermissionConfiguration {

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
            AuthorizationSubjectResolver authorizationSubjectResolver) {
        return new PlatformPageAccessService(pageAccessVerifier, authorizationSubjectResolver);
    }

    @Bean
    @Lazy
    PlatformPageAccessEndpoint platformPageAccessEndpoint(PlatformPageAccessService platformPageAccessService) {
        return new PlatformPageAccessEndpoint(platformPageAccessService);
    }
}
