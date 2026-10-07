package com.innospots.nexus.console.jaxrs.web;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleWebJerseySettingsTest {

    @Test
    void defaultRootPathIsOpenApiUi() {
        ConsoleWebJerseySettings settings = new ConsoleWebJerseySettings();
        assertThat(settings.getRootPath()).isEqualTo(OpenApiCatalogPaths.UI_DEFAULT);
        assertThat(settings.isForwardOn404()).isFalse();
    }
}
