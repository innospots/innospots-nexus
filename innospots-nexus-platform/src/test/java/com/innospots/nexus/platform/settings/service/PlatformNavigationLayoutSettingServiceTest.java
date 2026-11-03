package com.innospots.nexus.platform.settings.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformNavigationLayoutMode;
import com.innospots.nexus.platform.settings.domain.request.PlatformNavigationLayoutUpdateRequest;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformNavigationLayoutSettingServiceTest {

    @Test
    void getReturnsBootstrappedMode() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getOrBootstrap(
                        PlatformSettingKeys.NAVIGATION_LAYOUT,
                        null,
                        PlatformNavigationLayoutMode.RAIL_DUAL))
                .thenReturn(PlatformNavigationLayoutMode.SINGLE_GROUP);

        PlatformNavigationLayoutSettingService service = new PlatformNavigationLayoutSettingService(
                systemSettingService, PlatformNavigationLayoutMode.RAIL_DUAL);

        assertThat(service.getNavigationLayoutSetting().navigationLayoutMode())
                .isEqualTo(PlatformNavigationLayoutMode.SINGLE_GROUP);
    }

    @Test
    void updatePersistsMode() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(
                        PlatformSettingKeys.NAVIGATION_LAYOUT,
                        null,
                        PlatformNavigationLayoutMode.SINGLE_GROUP))
                .thenReturn(PlatformNavigationLayoutMode.SINGLE_GROUP);

        PlatformNavigationLayoutSettingService service = new PlatformNavigationLayoutSettingService(
                systemSettingService, PlatformNavigationLayoutMode.RAIL_DUAL);

        var result = service.updateNavigationLayoutSetting(new PlatformNavigationLayoutUpdateRequest(
                PlatformNavigationLayoutMode.SINGLE_GROUP));

        assertThat(result.navigationLayoutMode()).isEqualTo(PlatformNavigationLayoutMode.SINGLE_GROUP);
        verify(systemSettingService)
                .put(PlatformSettingKeys.NAVIGATION_LAYOUT, null, PlatformNavigationLayoutMode.SINGLE_GROUP);
    }
}
