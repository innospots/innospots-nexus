package com.innospots.nexus.platform.settings.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.status.PlatformConsoleUiSettingStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformConsoleGeneralSettingServiceTest {

    @Test
    void getConsoleGeneralSettingsBootstrapsFromDeploymentDefaultsWhenMissing() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getOrBootstrap(any(), eq(null), eq("Nexus Console")))
                .thenReturn("Nexus Console");
        when(systemSettingService.getOrBootstrap(any(), eq(null), eq("运营管理平台")))
                .thenReturn("运营管理平台");
        when(systemSettingService.getOrBootstrap(any(), eq(null), eq("zh-CN")))
                .thenReturn("zh-CN");
        when(systemSettingService.getOrBootstrap(any(), eq(null), eq("Asia/Shanghai")))
                .thenReturn("Asia/Shanghai");

        PlatformConsoleGeneralSettingService service = new PlatformConsoleGeneralSettingService(
                systemSettingService,
                "Nexus Console",
                "运营管理平台",
                "zh-CN",
                "Asia/Shanghai");

        assertThat(service.getConsoleGeneralSettings().siteName()).isEqualTo("Nexus Console");
        assertThat(service.getConsoleGeneralSettings().language()).isEqualTo("zh-CN");
    }

    @Test
    void updateSiteNameSettingPersistsTrimmedValue() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(any(), eq(null), eq("Acme")))
                .thenReturn("Acme");
        when(systemSettingService.getUpdatedAtOrBootstrap(any(), eq(null), eq("Nexus Console")))
                .thenReturn(null);

        PlatformConsoleGeneralSettingService service = new PlatformConsoleGeneralSettingService(
                systemSettingService,
                "Nexus Console",
                "",
                "zh-CN",
                "Asia/Shanghai");

        assertThat(service.updateSiteNameSetting(
                        new com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteNameUpdateRequest(
                                "  Acme  "))
                .siteName())
                .isEqualTo("Acme");
        verify(systemSettingService).put(any(), eq(null), eq("Acme"));
    }

    @Test
    void updateSiteNameSettingRejectsBlankName() {
        PlatformConsoleGeneralSettingService service = new PlatformConsoleGeneralSettingService(
                mock(SystemSettingService.class),
                "Nexus Console",
                "",
                "zh-CN",
                "Asia/Shanghai");

        assertThatThrownBy(() -> service.updateSiteNameSetting(
                        new com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteNameUpdateRequest(
                                "   ")))
                .isInstanceOf(com.innospots.nexus.base.exception.NexusException.class)
                .extracting(ex -> ((com.innospots.nexus.base.exception.NexusException) ex).code())
                .isEqualTo(PlatformConsoleUiSettingStatusCode.CONSOLE_SITE_NAME_INVALID.fullCode());
    }

    @Test
    void updateTimezoneSettingAcceptsAnyValidIanaZone() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(any(), eq(null), eq("Europe/Paris")))
                .thenReturn("Europe/Paris");
        when(systemSettingService.getUpdatedAtOrBootstrap(any(), eq(null), eq("Asia/Shanghai")))
                .thenReturn(null);

        PlatformConsoleGeneralSettingService service = new PlatformConsoleGeneralSettingService(
                systemSettingService,
                "Nexus Console",
                "",
                "zh-CN",
                "Asia/Shanghai");

        assertThat(service.updateTimezoneSetting(
                        new com.innospots.nexus.platform.settings.domain.request.PlatformConsoleTimezoneUpdateRequest(
                                "Europe/Paris"))
                .timezone())
                .isEqualTo("Europe/Paris");
    }

    @Test
    void updateTimezoneSettingRejectsInvalidZone() {
        PlatformConsoleGeneralSettingService service = new PlatformConsoleGeneralSettingService(
                mock(SystemSettingService.class),
                "Nexus Console",
                "",
                "zh-CN",
                "Asia/Shanghai");

        assertThatThrownBy(() -> service.updateTimezoneSetting(
                        new com.innospots.nexus.platform.settings.domain.request.PlatformConsoleTimezoneUpdateRequest(
                                "Not/AZone")))
                .isInstanceOf(com.innospots.nexus.base.exception.NexusException.class)
                .extracting(ex -> ((com.innospots.nexus.base.exception.NexusException) ex).code())
                .isEqualTo(PlatformConsoleUiSettingStatusCode.CONSOLE_TIMEZONE_INVALID.fullCode());
    }
}
