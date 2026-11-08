package com.innospots.nexus.console.user.admin.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

import com.innospots.nexus.console.user.admin.domain.request.AdminUserCreateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPageRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPasswordResetRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserStatusUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.vo.AdminUserVo;
import com.innospots.nexus.platform.user.endpoint.PlatformAdminUserEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAdminUserEndpointContractsTest {

    @Test
    void platformUserEndpointExposesAdminCreateWithoutPublicRegister() throws NoSuchMethodException {
        // 路径、权限组与标签由运营平台实现类绑定；契约接口只定义方法。
        assertThat(PlatformAdminUserEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(com.innospots.nexus.platform.config.PlatformConstant.USERS_PATH);
        assertThat(PlatformAdminUserEndpoint.class.getAnnotation(Tag.class).name()).isEqualTo("AdminUser");
        assertThat(PlatformAdminUserEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertHttpMethod(PlatformAdminUserEndpoint.class, "pageUsers", GET.class, AdminUserPageRequest.class);
        assertHttpMethod(PlatformAdminUserEndpoint.class, "createUser", POST.class, AdminUserCreateRequest.class);
        assertHttpMethod(PlatformAdminUserEndpoint.class, "getUser", GET.class, String.class);
        assertHttpMethod(
                PlatformAdminUserEndpoint.class,
                "updateUser",
                PUT.class,
                String.class,
                AdminUserUpdateRequest.class);
        assertHttpMethod(
                PlatformAdminUserEndpoint.class,
                "updateUserStatus",
                PUT.class,
                String.class,
                AdminUserStatusUpdateRequest.class);
        assertHttpMethod(
                PlatformAdminUserEndpoint.class,
                "resetUserPassword",
                PUT.class,
                String.class,
                AdminUserPasswordResetRequest.class);
        assertThat(Arrays.stream(AdminUserEndpoint.class.getMethods()).map(Method::getName))
                .doesNotContain("register");
        // 契约是接口，实现类才是 JAX-RS 资源。
        assertThat(AdminUserEndpoint.class.isInterface()).isTrue();
        assertThat(PlatformAdminUserEndpoint.class.isInterface()).isFalse();
    }

    @Test
    void platformUserRequestsAndViewsAreImmutableRecords() {
        assertThat(AdminUserCreateRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(AdminUserCreateRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "loginName",
                        "displayName",
                        "email",
                        "mobile",
                        "roleCodes",
                        "encryptedPassword",
                        "securityRealm");
        assertThat(AdminUserVo.class.isRecord()).isTrue();
        assertThat(Arrays.stream(AdminUserVo.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "adminUserId",
                        "securityRealm",
                        "loginName",
                        "displayName",
                        "email",
                        "mobile",
                        "status",
                        "lastLoginTime",
                        "lastLoginIp",
                        "createdAt");
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
