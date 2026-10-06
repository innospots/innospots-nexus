package com.innospots.nexus.platform.settings.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.setting.service.SystemSettingService;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.domain.request.PlatformRegistrationPolicyUpdateRequest;
import com.innospots.nexus.platform.settings.status.PlatformRegistrationModeSettingStatusCode;
import com.innospots.nexus.platform.settings.support.PlatformSettingKeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformRegistrationModeSettingServiceTest {

    @Test
    void getRegistrationModeSettingBootstrapsFromDeploymentDefaultWhenMissing() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.INVITE))
                .thenReturn(PlatformRegistrationMode.INVITE);
        when(systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE,
                        null,
                        PlatformRegistrationMode.INVITE.name()))
                .thenReturn(null);

        PlatformRegistrationModeSettingService service =
                new PlatformRegistrationModeSettingService(systemSettingService, PlatformRegistrationMode.INVITE);

        assertThat(service.getRegistrationModeSetting().registrationMode())
                .isEqualTo(PlatformRegistrationMode.INVITE);
        verify(systemSettingService)
                .getOrBootstrap(PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.INVITE);
    }

    @Test
    void requireSelfServiceModeRejectsMismatch() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.INVITE))
                .thenReturn(PlatformRegistrationMode.INVITE);

        PlatformRegistrationModeSettingService service =
                new PlatformRegistrationModeSettingService(systemSettingService, PlatformRegistrationMode.INVITE);

        assertThatThrownBy(() -> service.requireSelfServiceMode(PlatformRegistrationMode.OPEN))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformRegistrationModeSettingStatusCode.REGISTRATION_MODE_DISABLED.fullCode());
    }

    @Test
    void updateRegistrationModeSettingPersistsNewMode() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.APPROVAL))
                .thenReturn(PlatformRegistrationMode.APPROVAL);
        when(systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE,
                        null,
                        PlatformRegistrationMode.INVITE.name()))
                .thenReturn(null);

        PlatformRegistrationModeSettingService service =
                new PlatformRegistrationModeSettingService(systemSettingService, PlatformRegistrationMode.INVITE);

        assertThat(service.updateRegistrationModeSetting(
                        new PlatformRegistrationPolicyUpdateRequest(PlatformRegistrationMode.APPROVAL))
                        .registrationMode())
                .isEqualTo(PlatformRegistrationMode.APPROVAL);
        verify(systemSettingService).put(PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.APPROVAL);
    }

    @Test
    void updateRegistrationModeSettingWritesRequestedMode() {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.put(PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.OPEN))
                .thenReturn(PlatformRegistrationMode.OPEN);
        when(systemSettingService.getUpdatedAtOrBootstrap(
                        PlatformSettingKeys.REGISTRATION_MODE,
                        null,
                        PlatformRegistrationMode.INVITE.name()))
                .thenReturn(null);

        PlatformRegistrationModeSettingService service =
                new PlatformRegistrationModeSettingService(systemSettingService, PlatformRegistrationMode.INVITE);

        assertThat(service.updateRegistrationModeSetting(
                        new PlatformRegistrationPolicyUpdateRequest(PlatformRegistrationMode.OPEN))
                        .registrationMode())
                .isEqualTo(PlatformRegistrationMode.OPEN);
        verify(systemSettingService).put(PlatformSettingKeys.REGISTRATION_MODE, null, PlatformRegistrationMode.OPEN);
    }
}
