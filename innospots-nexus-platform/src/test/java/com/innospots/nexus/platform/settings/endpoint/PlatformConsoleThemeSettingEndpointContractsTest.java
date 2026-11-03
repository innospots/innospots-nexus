package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleThemeUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConsoleThemeSettingEndpointContractsTest {

    @Test
    void consoleThemeSettingEndpointIsAuthenticated() {
        assertThat(PlatformConsoleThemeSettingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.SETTINGS_CONSOLE_THEME_PATH);
        assertThat(PlatformConsoleThemeSettingEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformSettingsConsoleTheme");
        assertThat(PlatformConsoleThemeSettingEndpoint.class.getAnnotation(NexusAuthenticatedApi.class))
                .isNotNull();
    }

    @Test
    void consoleThemeSettingSupportsGetAndPut() throws NoSuchMethodException {
        assertThat(PlatformConsoleThemeSettingEndpoint.class.getMethod("getConsoleThemeSetting")
                        .getAnnotation(GET.class))
                .isNotNull();
        assertThat(PlatformConsoleThemeSettingEndpoint.class.getMethod(
                                "updateConsoleThemeSetting", PlatformConsoleThemeUpdateRequest.class)
                        .getAnnotation(PUT.class))
                .isNotNull();
    }
}
