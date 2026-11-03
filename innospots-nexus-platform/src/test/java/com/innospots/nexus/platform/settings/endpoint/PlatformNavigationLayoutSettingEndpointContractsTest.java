package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.settings.domain.request.PlatformNavigationLayoutUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformNavigationLayoutSettingEndpointContractsTest {

    @Test
    void navigationLayoutSettingEndpointIsAuthenticated() {
        assertThat(PlatformNavigationLayoutSettingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.SETTINGS_NAVIGATION_LAYOUT_PATH);
        assertThat(PlatformNavigationLayoutSettingEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformSettingsNavigationLayout");
        assertThat(PlatformNavigationLayoutSettingEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNotNull();
    }

    @Test
    void navigationLayoutSettingSupportsGetAndPut() throws NoSuchMethodException {
        assertThat(PlatformNavigationLayoutSettingEndpoint.class.getMethod("getNavigationLayoutSetting")
                        .getAnnotation(GET.class))
                .isNotNull();
        assertThat(PlatformNavigationLayoutSettingEndpoint.class.getMethod(
                                "updateNavigationLayoutSetting",
                                PlatformNavigationLayoutUpdateRequest.class)
                        .getAnnotation(PUT.class))
                .isNotNull();
    }
}
