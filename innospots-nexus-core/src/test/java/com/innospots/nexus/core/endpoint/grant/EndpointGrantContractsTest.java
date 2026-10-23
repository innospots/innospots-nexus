package com.innospots.nexus.core.endpoint.grant;

import static org.assertj.core.api.Assertions.assertThat;

import com.innospots.nexus.core.endpoint.contract.ConsoleRestPaths;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import org.junit.jupiter.api.Test;

class EndpointGrantContractsTest {

    @Test
    void grantTypesAndRestPathConstants() {
        assertThat(ConsoleRestPaths.API_PREFIX).isEqualTo("/api/d/nexus");
        assertThat(ConsoleRestPaths.PUBLIC_API_PREFIX).isEqualTo("/api/public");
        assertThat(ResolvedEndpointGrant.class).isRecord();
        assertThat(EndpointGrant.class.isAnnotation()).isTrue();
        assertThat(EndpointModule.class.isAnnotation()).isTrue();
        assertThat(I18nLiteral.class.isAnnotation()).isTrue();
    }
}
