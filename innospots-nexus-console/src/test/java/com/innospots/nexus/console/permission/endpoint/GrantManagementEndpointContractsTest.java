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
    void grantManagementUsesRoleAndOrganizationUnitSubjects() throws NoSuchMethodException {
        assertThat(GrantManagementEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/console");
        assertHttpMethod(GrantManagementEndpoint.class, "getRolePermissions", GET.class, String.class);
        assertHttpMethod(GrantManagementEndpoint.class, "replaceRolePermissions",
                PUT.class, String.class, PermissionGrantReplaceRequest.class);
        assertHttpMethod(GrantManagementEndpoint.class, "getOrganizationUnitPermissions",
                GET.class, String.class);
        assertHttpMethod(GrantManagementEndpoint.class, "replaceOrganizationUnitPermissions",
                PUT.class, String.class, PermissionGrantReplaceRequest.class);

        Method orgGet = GrantManagementEndpoint.class.getMethod(
                "getOrganizationUnitPermissions", String.class);
        assertThat(orgGet.getAnnotation(Path.class).value())
                .isEqualTo("/organization-units/{unitId}/permissions");
        Method orgPut = GrantManagementEndpoint.class.getMethod(
                "replaceOrganizationUnitPermissions", String.class, PermissionGrantReplaceRequest.class);
        assertThat(orgPut.getAnnotation(Path.class).value())
                .isEqualTo("/organization-units/{unitId}/permissions");

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
