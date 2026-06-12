package com.innospots.nexus.kernel.role.endpoint;

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
import com.innospots.nexus.kernel.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleMemberAddRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleMemberPageRequest;
import com.innospots.nexus.kernel.role.domain.request.RolePageRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.kernel.role.domain.vo.RoleMemberVo;
import com.innospots.nexus.kernel.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.kernel.role.domain.vo.RoleVo;

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
    void roleMemberEndpointRemainsASeparateBoundary() throws NoSuchMethodException {
        assertThat(RoleMemberEndpoint.class.isInterface()).isFalse();
        assertThat(RoleMemberEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/roles/{roleId}/members");
        assertHttpMethod(RoleMemberEndpoint.class, "pageRoleMembers",
                GET.class, String.class, RoleMemberPageRequest.class);
        assertHttpMethod(RoleMemberEndpoint.class, "addRoleMembers",
                POST.class, String.class, RoleMemberAddRequest.class);
        assertHttpMethod(RoleMemberEndpoint.class, "removeRoleMember",
                DELETE.class, String.class, String.class);
    }

    @Test
    void roleMemberEndpointMethodsRemainExplicitlyUnimplemented() {
        RoleMemberEndpoint endpoint = new RoleMemberEndpoint();

        assertThatThrownBy(() -> endpoint.pageRoleMembers("role-1", new RoleMemberPageRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> endpoint.addRoleMembers("role-1", new RoleMemberAddRequest(List.of("user-1"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> endpoint.removeRoleMember("role-1", "user-1"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void roleRequestsAndViewsAreImmutableRecords() {
        assertRecordComponents(RoleCreateRequest.class,
                "roleName", "roleCode", "description", "sortOrder");
        assertRecordComponents(RoleUpdateRequest.class,
                "roleName", "description", "sortOrder");
        assertRecordComponents(RoleStatusUpdateRequest.class, "status");
        assertRecordComponents(RolePageRequest.class,
                "input", "status", "builtIn", "pageNo", "pageSize");
        assertRecordComponents(RoleMemberPageRequest.class,
                "input", "pageNo", "pageSize");
        assertRecordComponents(RoleMemberAddRequest.class, "userIds");

        assertThat(RoleVo.class.isRecord()).isTrue();
        assertThat(RoleOptionVo.class.isRecord()).isTrue();
        assertThat(RoleMemberVo.class.isRecord()).isTrue();
    }

    @Test
    void collectionRequestsDefensivelyCopyInput() {
        RoleMemberAddRequest memberRequest = new RoleMemberAddRequest(null);

        assertThat(memberRequest.userIds()).isEqualTo(List.of());
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
