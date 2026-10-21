package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.permission.endpoint.PageAccessEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPageAccessEndpointContractsTest {

    @Test
    void implementsConsolePageAccessEndpointOnPublicPath() {
        assertThat(PageAccessEndpoint.class.isAssignableFrom(PlatformPageAccessEndpoint.class)).isTrue();
        assertThat(PlatformPageAccessEndpoint.class.getAnnotation(Path.class)).isNull();
        assertThat(PageAccessEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(ConsoleConstant.PUBLIC_API_PREFIX + "/page-access");
    }
}
