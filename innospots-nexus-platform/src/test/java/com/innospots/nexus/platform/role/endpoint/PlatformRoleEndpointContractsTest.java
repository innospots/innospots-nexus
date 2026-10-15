package com.innospots.nexus.platform.role.endpoint;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformRoleEndpointContractsTest {

    @Test
    void implementsConsoleRoleEndpointOnPlatformPath() {
        assertThat(RoleEndpoint.class.isAssignableFrom(PlatformRoleEndpoint.class)).isTrue();
        assertThat(PlatformRoleEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.ROLES_PATH);
    }

    @Test
    void implementsConsoleRoleBindingEndpointOnPlatformPath() {
        assertThat(RoleBindingEndpoint.class.isAssignableFrom(PlatformRoleBindingEndpoint.class)).isTrue();
        assertThat(PlatformRoleBindingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.ROLE_BINDINGS_PATH);
    }
}
