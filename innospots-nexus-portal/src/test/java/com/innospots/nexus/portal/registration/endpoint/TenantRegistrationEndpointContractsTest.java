package com.innospots.nexus.portal.registration.endpoint;

import java.lang.reflect.Method;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import org.eclipse.microprofile.openapi.annotations.Operation;

import com.innospots.nexus.portal.registration.domain.request.TenantRegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRegistrationEndpointContractsTest {

    @Test
    void tenantRegistrationEndpointProvidesIndependentRegisterContract() throws NoSuchMethodException {
        assertThat(TenantRegistrationEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/tenant/registration");

        Method register = TenantRegistrationEndpoint.class.getMethod(
                "register", TenantRegisterRequest.class);
        assertThat(register.getAnnotation(POST.class)).isNotNull();

        Operation operation = register.getAnnotation(Operation.class);
        assertThat(operation).isNotNull();
        assertThat(operation.operationId()).isEqualTo("tenantRegistrationRegister");
    }

    @Test
    void authEndpointNoLongerExposesRegister() {
        assertThat(TenantRegistrationEndpoint.class.getPackageName())
                .isEqualTo("com.innospots.nexus.portal.registration.endpoint");
        assertThat(TenantRegistrationEndpoint.class.getAnnotation(Path.class).value())
                .doesNotStartWith("/tenant/auth");
    }
}
