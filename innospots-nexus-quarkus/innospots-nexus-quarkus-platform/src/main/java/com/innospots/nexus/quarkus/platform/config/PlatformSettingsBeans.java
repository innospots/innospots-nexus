package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformConsoleTheme;
import com.innospots.nexus.platform.settings.domain.enums.PlatformNavigationLayoutMode;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;
import com.innospots.nexus.platform.settings.service.PlatformConsoleThemeSettingService;
import com.innospots.nexus.platform.settings.service.PlatformNavigationLayoutSettingService;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

@ApplicationScoped
public class PlatformSettingsBeans {

    @Produces
    @Singleton
    PlatformRegistrationModeSettingService platformRegistrationModeSettingService(
            SystemSettingService systemSettingService,
            @ConfigProperty(name = "nexus.platform.registration.mode", defaultValue = "INVITE")
                    String deploymentRegistrationMode) {
        PlatformRegistrationMode defaultMode =
                PlatformRegistrationModeSettingService.parseDeploymentDefault(deploymentRegistrationMode);
        return new PlatformRegistrationModeSettingService(systemSettingService, defaultMode);
    }

    @Produces
    @Singleton
    PlatformConsoleGeneralSettingService platformConsoleGeneralSettingService(
            SystemSettingService systemSettingService,
            @ConfigProperty(name = "nexus.platform.console.site-name", defaultValue = "Nexus Console")
                    String deploymentSiteName,
            @ConfigProperty(name = "nexus.platform.console.site-description", defaultValue = "运营管理平台")
                    String deploymentSiteDescription,
            @ConfigProperty(name = "nexus.platform.console.display-language", defaultValue = "zh-CN")
                    String deploymentDisplayLanguage,
            @ConfigProperty(name = "nexus.platform.console.timezone", defaultValue = "Asia/Shanghai")
                    String deploymentTimezone) {
        return new PlatformConsoleGeneralSettingService(
                systemSettingService,
                PlatformConsoleGeneralSettingService.parseDeploymentSiteName(deploymentSiteName),
                PlatformConsoleGeneralSettingService.parseDeploymentSiteDescription(deploymentSiteDescription),
                PlatformConsoleGeneralSettingService.parseDeploymentDisplayLanguage(deploymentDisplayLanguage),
                PlatformConsoleGeneralSettingService.parseDeploymentTimezone(deploymentTimezone));
    }

    @Produces
    @Singleton
    PlatformConsoleThemeSettingService platformConsoleThemeSettingService(
            SystemSettingService systemSettingService,
            @ConfigProperty(name = "nexus.platform.console.theme", defaultValue = "CHENWUNAIFU")
                    String deploymentConsoleTheme) {
        PlatformConsoleTheme defaultTheme =
                PlatformConsoleThemeSettingService.parseDeploymentDefault(deploymentConsoleTheme);
        return new PlatformConsoleThemeSettingService(systemSettingService, defaultTheme);
    }

    @Produces
    @Singleton
    PlatformNavigationLayoutSettingService platformNavigationLayoutSettingService(
            SystemSettingService systemSettingService,
            @ConfigProperty(
                            name = "nexus.platform.console.navigation-layout",
                            defaultValue = "RAIL_DUAL")
                    String deploymentNavigationLayout) {
        PlatformNavigationLayoutMode defaultMode =
                PlatformNavigationLayoutSettingService.parseDeploymentDefault(deploymentNavigationLayout);
        return new PlatformNavigationLayoutSettingService(systemSettingService, defaultMode);
    }
}
