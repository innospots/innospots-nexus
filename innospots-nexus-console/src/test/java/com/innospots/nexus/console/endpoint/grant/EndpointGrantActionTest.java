package com.innospots.nexus.console.endpoint.grant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointGrantActionTest {

    @Test
    void exposesStableActionCatalog() {
        assertThat(EndpointGrantAction.values()).containsExactly(
                EndpointGrantAction.READ,
                EndpointGrantAction.CREATE,
                EndpointGrantAction.UPDATE,
                EndpointGrantAction.DELETE,
                EndpointGrantAction.STATUS,
                EndpointGrantAction.REPLACE,
                EndpointGrantAction.EXECUTE,
                EndpointGrantAction.EXPORT,
                EndpointGrantAction.IMPORT,
                EndpointGrantAction.ASSIGN,
                EndpointGrantAction.REVOKE,
                EndpointGrantAction.WRITE);
    }
}
