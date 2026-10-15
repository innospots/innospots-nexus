package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.endpoint.CurrentAuthorizationEndpoint;
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.console.permission.endpoint.OrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPermissionEndpointContractsTest {

    @Test
    void implementsConsolePermissionEndpointsOnPlatformPaths() {
        assertThat(GrantManagementEndpoint.class.isAssignableFrom(PlatformGrantManagementEndpoint.class)).isTrue();
        assertThat(PlatformGrantManagementEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.ROLE_PERMISSIONS_PATH);

        assertThat(OrganizationUnitGrantManagementEndpoint.class.isAssignableFrom(
                PlatformOrganizationUnitGrantManagementEndpoint.class)).isTrue();
        assertThat(PlatformOrganizationUnitGrantManagementEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.ORGANIZATION_UNIT_PERMISSIONS_PATH);

        assertThat(CurrentAuthorizationEndpoint.class.isAssignableFrom(PlatformCurrentAuthorizationEndpoint.class))
                .isTrue();
        assertThat(PlatformCurrentAuthorizationEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.ME_PERMISSIONS_PATH);
    }
}
