package com.innospots.nexus.platform.organization.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.organization.domain.request.EnterpriseProfileUpsertRequest;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseProfileEndpointContractsTest {

    @Test
    void enterpriseProfileEndpointExposesSeparateTenantEnterpriseApi() throws NoSuchMethodException {
        assertThat(EnterpriseProfileEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.TENANT_ENTERPRISE_PATH);
        assertThat(EnterpriseProfileEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformEnterpriseProfile");
        assertThat(EnterpriseProfileEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertThat(EnterpriseProfileEndpoint.class
                .getMethod("upsertEnterpriseProfile", String.class, EnterpriseProfileUpsertRequest.class)
                .getAnnotation(Operation.class)
                .operationId()).isEqualTo("platformTenantEnterpriseUpsert");
        assertThat(EnterpriseProfileEndpoint.class
                .getMethod("getEnterpriseProfile", String.class)
                .getAnnotation(GET.class)).isNotNull();
        assertThat(EnterpriseProfileEndpoint.class
                .getMethod("upsertEnterpriseProfile", String.class, EnterpriseProfileUpsertRequest.class)
                .getAnnotation(PUT.class)).isNotNull();
    }
}
