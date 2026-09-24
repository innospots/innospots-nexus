package com.innospots.nexus.portal.scope.endpoint;

import java.lang.reflect.Method;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.portal.scope.domain.request.SelectProjectRequest;
import com.innospots.nexus.portal.scope.domain.request.SelectWorkspaceRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantScopeEndpointContractsTest {

    @Test
    void tenantScopeEndpointSupportsWorkspaceAndProjectSelection() throws NoSuchMethodException {
        assertThat(TenantScopeEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/tenant/scope");
        assertHttpMethod(TenantScopeEndpoint.class, "selectWorkspace", POST.class, SelectWorkspaceRequest.class);
        assertHttpMethod(TenantScopeEndpoint.class, "selectProject", POST.class, SelectProjectRequest.class);
    }

    private static void assertHttpMethod(
            Class<?> endpointType,
            String methodName,
            Class<? extends java.lang.annotation.Annotation> httpAnnotation,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = endpointType.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(httpAnnotation)).isNotNull();
    }
}
