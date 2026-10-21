package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.endpoint.PageAccessEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPageAccessEndpointContractsTest {

    @Test
    void implementsConsolePageAccessEndpointOnPlatformPath() {
        assertThat(PageAccessEndpoint.class.isAssignableFrom(PlatformPageAccessEndpoint.class)).isTrue();
        assertThat(PlatformPageAccessEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PAGE_ACCESS_VERIFY_PATH);
    }
}
