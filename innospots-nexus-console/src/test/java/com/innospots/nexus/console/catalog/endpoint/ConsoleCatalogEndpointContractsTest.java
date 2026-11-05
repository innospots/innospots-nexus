package com.innospots.nexus.console.catalog.endpoint;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleCatalogEndpointContractsTest {

    @Test
    void catalogReadsRequireAuthenticationButIgnoreBusinessPermission() throws NoSuchMethodException {
        assertThat(ConsoleCatalogEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();

        assertReadGrant("permissionCatalog");
        assertReadGrant("tree");
    }

    private static void assertReadGrant(String methodName) throws NoSuchMethodException {
        Method method = ConsoleCatalogEndpoint.class.getMethod(methodName);
        EndpointGrant grant = method.getAnnotation(EndpointGrant.class);

        assertThat(grant).isNotNull();
        assertThat(grant.ignoreAuth()).isFalse();
        assertThat(grant.ignorePermission()).isTrue();
    }
}
