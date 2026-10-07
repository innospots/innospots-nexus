package com.innospots.nexus.platform.user.endpoint;

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

import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserPageRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserStatusUpdateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserUpdateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUserEndpointContractsTest {

    @Test
    void platformUserEndpointExposesAdminCreateWithoutPublicRegister() throws NoSuchMethodException {
        assertThat(PlatformUserEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(com.innospots.nexus.platform.config.PlatformConstant.USERS_PATH);
        assertThat(PlatformUserEndpoint.class.getAnnotation(Tag.class).name()).isEqualTo("PlatformUser");
        assertThat(PlatformUserEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertThat(PlatformUserEndpoint.class.getMethod("createUser", PlatformUserCreateRequest.class)
                .getAnnotation(Operation.class).operationId()).isEqualTo("platformUserCreate");
        assertHttpMethod(PlatformUserEndpoint.class, "pageUsers", GET.class, PlatformUserPageRequest.class);
        assertHttpMethod(PlatformUserEndpoint.class, "createUser", POST.class, PlatformUserCreateRequest.class);
        assertHttpMethod(PlatformUserEndpoint.class, "getUser", GET.class, String.class);
        assertHttpMethod(
                PlatformUserEndpoint.class,
                "updateUser",
                PUT.class,
                String.class,
                PlatformUserUpdateRequest.class);
        assertHttpMethod(
                PlatformUserEndpoint.class,
                "updateUserStatus",
                PUT.class,
                String.class,
                PlatformUserStatusUpdateRequest.class);
        assertThat(Arrays.stream(PlatformUserEndpoint.class.getMethods()).map(Method::getName))
                .doesNotContain("register");
        assertThat(PlatformUserEndpoint.class.isInterface()).isFalse();
    }

    @Test
    void platformUserRequestsAndViewsAreImmutableRecords() {
        assertThat(PlatformUserCreateRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(PlatformUserCreateRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "loginName",
                        "displayName",
                        "email",
                        "mobile",
                        "employeeNo",
                        "roleCodes",
                        "encryptedPassword");
        assertThat(PlatformUserVo.class.isRecord()).isTrue();
        assertThat(Arrays.stream(PlatformUserVo.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "platformUserId",
                        "loginName",
                        "displayName",
                        "email",
                        "mobile",
                        "employeeNo",
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
