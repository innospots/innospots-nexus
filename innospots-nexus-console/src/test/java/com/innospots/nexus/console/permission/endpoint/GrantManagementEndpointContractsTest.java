package com.innospots.nexus.console.permission.endpoint;

import java.lang.reflect.Method;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GrantManagementEndpointContractsTest {

    @Test
    void grantManagementEndpointsAreContractsWithoutClassPath() throws NoSuchMethodException {
        assertThat(GrantManagementEndpoint.class.isInterface()).isTrue();
        assertThat(GrantManagementEndpoint.class.getAnnotation(Path.class)).isNull();
        assertHttpMethod(GrantManagementEndpoint.class, "getRolePermissions", GET.class, String.class);
        assertHttpMethod(GrantManagementEndpoint.class, "replaceRolePermissions",
                PUT.class, String.class, PermissionGrantReplaceRequest.class);

        assertThat(OrganizationUnitGrantManagementEndpoint.class.isInterface()).isTrue();
        assertThat(OrganizationUnitGrantManagementEndpoint.class.getAnnotation(Path.class)).isNull();
        assertHttpMethod(OrganizationUnitGrantManagementEndpoint.class, "getOrganizationUnitPermissions",
                GET.class, String.class);
        assertHttpMethod(OrganizationUnitGrantManagementEndpoint.class, "replaceOrganizationUnitPermissions",
                PUT.class, String.class, PermissionGrantReplaceRequest.class);

        assertThat(CurrentAuthorizationEndpoint.class.isInterface()).isTrue();
        assertThat(CurrentAuthorizationEndpoint.class.getAnnotation(Path.class)).isNull();
        assertHttpMethod(CurrentAuthorizationEndpoint.class, "listResources", GET.class);

        assertThat(Arrays.stream(GrantManagementEndpoint.class.getMethods()).map(Method::getName))
                .doesNotContain("getGroupPermissions", "replaceGroupPermissions");
        assertThat(PermissionSubjectType.values()).containsExactly(
                PermissionSubjectType.ROLE, PermissionSubjectType.ORG_UNIT);
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
