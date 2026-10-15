package com.innospots.nexus.platform.navigation.endpoint;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformNavigationMenuEndpointContractsTest {

    @Test
    void implementsConsoleNavigationMenuEndpointOnPlatformPath() {
        assertThat(NavigationMenuEndpoint.class.isAssignableFrom(PlatformNavigationMenuEndpoint.class)).isTrue();
        assertThat(PlatformNavigationMenuEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.NAVIGATION_MENUS_PATH);
    }
}
