package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
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
}
