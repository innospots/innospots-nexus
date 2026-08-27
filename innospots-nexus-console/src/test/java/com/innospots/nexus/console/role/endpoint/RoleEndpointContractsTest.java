package com.innospots.nexus.console.role.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleEndpointContractsTest {

    @Test
    void roleEndpointKeepsRoleLifecycleOperationsTogether() throws NoSuchMethodException {
        assertThat(RoleEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/console/roles");
        assertHttpMethod(RoleEndpoint.class, "pageRoles", GET.class, RolePageRequest.class);
        assertHttpMethod(RoleEndpoint.class, "getRole", GET.class, String.class);
        assertHttpMethod(RoleEndpoint.class, "createRole", POST.class, RoleCreateRequest.class);
        assertHttpMethod(RoleEndpoint.class, "updateRole", PUT.class, String.class, RoleUpdateRequest.class);
        assertHttpMethod(RoleEndpoint.class, "updateRoleStatus",
                PUT.class, String.class, RoleStatusUpdateRequest.class);
        assertHttpMethod(RoleEndpoint.class, "deleteRole", DELETE.class, String.class);
        assertHttpMethod(RoleEndpoint.class, "listRoleOptions", GET.class, BasicStatus.class);
    }

    @Test
    void roleBindingEndpointRemainsASeparateBoundary() throws NoSuchMethodException {
        assertThat(RoleBindingEndpoint.class.isInterface()).isFalse();
        assertThat(RoleBindingEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/roles/{roleId}/bindings");
        assertHttpMethod(RoleBindingEndpoint.class, "pageRoleBindings",
                GET.class, String.class, RoleBindingPageRequest.class);
        assertHttpMethod(RoleBindingEndpoint.class, "addRoleBindings",
                POST.class, String.class, RoleBindingAddRequest.class);
        assertHttpMethod(RoleBindingEndpoint.class, "removeRoleBinding",
                DELETE.class, String.class, String.class);
    }

    @Test
    void roleBindingEndpointMethodsRemainExplicitlyUnimplemented() {
        RoleBindingEndpoint endpoint = new RoleBindingEndpoint();

        assertThatThrownBy(() -> endpoint.pageRoleBindings("role-1", new RoleBindingPageRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> endpoint.addRoleBindings(
                "role-1",
                new RoleBindingAddRequest(RoleBindingSubjectType.USER, List.of("user-1"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> endpoint.removeRoleBinding("role-1", "rbn-1"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void roleRequestsAndViewsAreImmutableRecords() {
        assertRecordComponents(RoleCreateRequest.class,
                "roleName", "roleCode", "ownerType", "ownerId", "securityRealm", "description", "sortOrder");
        assertRecordComponents(RoleUpdateRequest.class,
                "roleName", "description", "sortOrder");
        assertRecordComponents(RoleStatusUpdateRequest.class, "status");
        assertRecordComponents(RolePageRequest.class,
                "input", "status", "builtIn", "pageNo", "pageSize");
        assertRecordComponents(RoleBindingPageRequest.class,
                "input", "subjectType", "pageNo", "pageSize");
        assertRecordComponents(RoleBindingAddRequest.class, "subjectType", "subjectIds");

        assertThat(RoleVo.class.isRecord()).isTrue();
        assertThat(RoleOptionVo.class.isRecord()).isTrue();
        assertThat(RoleBindingVo.class.isRecord()).isTrue();
    }

    @Test
    void collectionRequestsDefensivelyCopyInput() {
        RoleBindingAddRequest bindingRequest = new RoleBindingAddRequest(RoleBindingSubjectType.USER, null);

        assertThat(bindingRequest.subjectIds()).isEqualTo(List.of());
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

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(names);
    }
}
