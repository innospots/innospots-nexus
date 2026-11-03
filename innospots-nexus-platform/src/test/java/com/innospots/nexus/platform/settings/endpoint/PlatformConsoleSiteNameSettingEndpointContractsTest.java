package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteNameUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConsoleSiteNameSettingEndpointContractsTest {

    @Test
    void consoleSiteNameSettingEndpointIsAuthenticated() {
        assertThat(PlatformConsoleSiteNameSettingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.SETTINGS_CONSOLE_SITE_NAME_PATH);
        assertThat(PlatformConsoleSiteNameSettingEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformSettingsConsoleSiteName");
        assertThat(PlatformConsoleSiteNameSettingEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNotNull();
    }

    @Test
    void consoleSiteNameSettingSupportsGetAndPut() throws NoSuchMethodException {
        assertThat(PlatformConsoleSiteNameSettingEndpoint.class.getMethod("getSiteNameSetting")
                        .getAnnotation(GET.class))
                .isNotNull();
        assertThat(PlatformConsoleSiteNameSettingEndpoint.class.getMethod(
                                "updateSiteNameSetting", PlatformConsoleSiteNameUpdateRequest.class)
                        .getAnnotation(PUT.class))
                .isNotNull();
    }
}
