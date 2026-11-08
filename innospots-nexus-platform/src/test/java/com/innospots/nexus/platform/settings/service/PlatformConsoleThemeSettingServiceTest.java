package com.innospots.nexus.platform.settings.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformConsoleTheme;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleThemeUpdateRequest;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformConsoleThemeSettingServiceTest {

    @Test
    void getReturnsBootstrappedTheme() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getOrBootstrap(
                        PlatformSettingKeys.CONSOLE_THEME, null, PlatformConsoleTheme.CHENWUNAIFU))
                .thenReturn(PlatformConsoleTheme.TAIJINGSHENLIN);

        PlatformConsoleThemeSettingService service =
                new PlatformConsoleThemeSettingService(systemSettingService, PlatformConsoleTheme.CHENWUNAIFU);

        assertThat(service.getConsoleThemeSetting().theme()).isEqualTo(PlatformConsoleTheme.TAIJINGSHENLIN);
        assertThat(service.getConsoleThemeSetting().skinKey()).isEqualTo("bi-shan");
    }

    @Test
    void updatePersistsTheme() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(
                        PlatformSettingKeys.CONSOLE_THEME, null, PlatformConsoleTheme.XUANZHIMOYUN))
                .thenReturn(PlatformConsoleTheme.XUANZHIMOYUN);

        PlatformConsoleThemeSettingService service =
                new PlatformConsoleThemeSettingService(systemSettingService, PlatformConsoleTheme.CHENWUNAIFU);

        var result = service.updateConsoleThemeSetting(
                new PlatformConsoleThemeUpdateRequest(PlatformConsoleTheme.XUANZHIMOYUN));

        assertThat(result.theme()).isEqualTo(PlatformConsoleTheme.XUANZHIMOYUN);
        verify(systemSettingService).put(PlatformSettingKeys.CONSOLE_THEME, null, PlatformConsoleTheme.XUANZHIMOYUN);
    }
}
