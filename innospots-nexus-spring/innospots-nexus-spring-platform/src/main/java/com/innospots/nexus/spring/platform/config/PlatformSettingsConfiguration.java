package com.innospots.nexus.spring.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleThemeSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformNavigationLayoutSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformRegistrationModeSettingEndpoint;
import com.innospots.nexus.platform.settings.service.PlatformConsoleThemeSettingService;
import com.innospots.nexus.platform.settings.service.PlatformNavigationLayoutSettingService;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

@Configuration
public class PlatformSettingsConfiguration {

    @Bean
    PlatformRegistrationModeSettingService platformRegistrationModeSettingService(
            SystemSettingService systemSettingService,
            @Value("${nexus.platform.registration.mode:INVITE}") String deploymentRegistrationMode) {
        PlatformRegistrationMode defaultMode =
                PlatformRegistrationModeSettingService.parseDeploymentDefault(deploymentRegistrationMode);
        return new PlatformRegistrationModeSettingService(systemSettingService, defaultMode);
    }

    @Bean
    @Lazy
    PlatformRegistrationModeSettingEndpoint platformRegistrationModeSettingEndpoint(
            PlatformRegistrationModeSettingService registrationModeSettingService) {
        return new PlatformRegistrationModeSettingEndpoint(registrationModeSettingService);
    }

    @Bean
    PlatformConsoleThemeSettingService platformConsoleThemeSettingService(
            SystemSettingService systemSettingService,
            @Value("${nexus.platform.console.theme:CHENWUNAIFU}") String deploymentConsoleTheme) {
        return new PlatformConsoleThemeSettingService(
                systemSettingService,
                PlatformConsoleThemeSettingService.parseDeploymentDefault(deploymentConsoleTheme));
    }

    @Bean
    PlatformNavigationLayoutSettingService platformNavigationLayoutSettingService(
            SystemSettingService systemSettingService,
            @Value("${nexus.platform.console.navigation-layout:RAIL_DUAL}") String deploymentNavigationLayout) {
        return new PlatformNavigationLayoutSettingService(
                systemSettingService,
                PlatformNavigationLayoutSettingService.parseDeploymentDefault(deploymentNavigationLayout));
    }

    @Bean
    @Lazy
    PlatformConsoleThemeSettingEndpoint platformConsoleThemeSettingEndpoint(
            PlatformConsoleThemeSettingService consoleThemeSettingService) {
        return new PlatformConsoleThemeSettingEndpoint(consoleThemeSettingService);
    }

    @Bean
    @Lazy
    PlatformNavigationLayoutSettingEndpoint platformNavigationLayoutSettingEndpoint(
            PlatformNavigationLayoutSettingService navigationLayoutSettingService) {
        return new PlatformNavigationLayoutSettingEndpoint(navigationLayoutSettingService);
    }
}
