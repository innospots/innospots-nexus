package com.innospots.nexus.spring.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleDisplayLanguageSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleGeneralSettingsEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteDescriptionSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteNameSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleThemeSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleTimezoneSettingEndpoint;
import com.innospots.nexus.platform.settings.endpoint.PlatformNavigationLayoutSettingEndpoint;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;
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
    PlatformConsoleGeneralSettingService platformConsoleGeneralSettingService(
            SystemSettingService systemSettingService,
            @Value("${nexus.platform.console.site-name:Nexus Console}") String deploymentSiteName,
            @Value("${nexus.platform.console.site-description:运营管理平台}") String deploymentSiteDescription,
            @Value("${nexus.platform.console.display-language:zh-CN}") String deploymentDisplayLanguage,
            @Value("${nexus.platform.console.timezone:Asia/Shanghai}") String deploymentTimezone) {
        return new PlatformConsoleGeneralSettingService(
                systemSettingService,
                PlatformConsoleGeneralSettingService.parseDeploymentSiteName(deploymentSiteName),
                PlatformConsoleGeneralSettingService.parseDeploymentSiteDescription(deploymentSiteDescription),
                PlatformConsoleGeneralSettingService.parseDeploymentDisplayLanguage(deploymentDisplayLanguage),
                PlatformConsoleGeneralSettingService.parseDeploymentTimezone(deploymentTimezone));
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
    PlatformConsoleGeneralSettingsEndpoint platformConsoleGeneralSettingsEndpoint(
            PlatformConsoleGeneralSettingService consoleGeneralSettingService) {
        return new PlatformConsoleGeneralSettingsEndpoint(consoleGeneralSettingService);
    }

    @Bean
    @Lazy
    PlatformConsoleSiteNameSettingEndpoint platformConsoleSiteNameSettingEndpoint(
            PlatformConsoleGeneralSettingService consoleGeneralSettingService) {
        return new PlatformConsoleSiteNameSettingEndpoint(consoleGeneralSettingService);
    }

    @Bean
    @Lazy
    PlatformConsoleSiteDescriptionSettingEndpoint platformConsoleSiteDescriptionSettingEndpoint(
            PlatformConsoleGeneralSettingService consoleGeneralSettingService) {
        return new PlatformConsoleSiteDescriptionSettingEndpoint(consoleGeneralSettingService);
    }

    @Bean
    @Lazy
    PlatformConsoleDisplayLanguageSettingEndpoint platformConsoleDisplayLanguageSettingEndpoint(
            PlatformConsoleGeneralSettingService consoleGeneralSettingService) {
        return new PlatformConsoleDisplayLanguageSettingEndpoint(consoleGeneralSettingService);
    }

    @Bean
    @Lazy
    PlatformConsoleTimezoneSettingEndpoint platformConsoleTimezoneSettingEndpoint(
            PlatformConsoleGeneralSettingService consoleGeneralSettingService) {
        return new PlatformConsoleTimezoneSettingEndpoint(consoleGeneralSettingService);
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
