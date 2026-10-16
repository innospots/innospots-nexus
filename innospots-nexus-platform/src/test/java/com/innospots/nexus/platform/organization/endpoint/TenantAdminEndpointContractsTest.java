package com.innospots.nexus.platform.organization.endpoint;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAdminEndpointContractsTest {

    @Test
    void tenantAdminEndpointExposesListAddAndRemove() throws NoSuchMethodException {
        assertThat(TenantAdminEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.TENANT_ADMINS_PATH);
        assertThat(TenantAdminEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertThat(TenantAdminEndpoint.class.getMethod("listAdmins", String.class).getAnnotation(GET.class))
                .isNotNull();
        assertThat(TenantAdminEndpoint.class
                .getMethod("addAdmin", String.class,
                        com.innospots.nexus.platform.organization.domain.request.TenantAdminAddRequest.class)
                .getAnnotation(POST.class))
                .isNotNull();
        assertThat(TenantAdminEndpoint.class.getMethod("removeAdmin", String.class, String.class)
                .getAnnotation(DELETE.class))
                .isNotNull();
    }
}
