package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.settings.domain.request.PlatformRegistrationPolicyUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformRegistrationModeSettingEndpointContractsTest {

    @Test
    void adminRegistrationModeSettingEndpointIsAuthenticated() {
        assertThat(PlatformRegistrationModeSettingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH);
        assertThat(PlatformRegistrationModeSettingEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformSettingsRegistrationMode");
        assertThat(PlatformRegistrationModeSettingEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNotNull();
    }

    @Test
    void adminRegistrationModeSettingSupportsGetAndPut() throws NoSuchMethodException {
        assertThat(PlatformRegistrationModeSettingEndpoint.class.getMethod("getRegistrationModeSetting")
                .getAnnotation(GET.class)).isNotNull();
        assertThat(PlatformRegistrationModeSettingEndpoint.class.getMethod(
                        "updateRegistrationModeSetting", PlatformRegistrationPolicyUpdateRequest.class)
                .getAnnotation(PUT.class)).isNotNull();
    }
}
