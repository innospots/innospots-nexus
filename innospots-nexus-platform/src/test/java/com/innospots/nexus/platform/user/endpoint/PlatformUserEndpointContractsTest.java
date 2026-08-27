package com.innospots.nexus.platform.user.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUserEndpointContractsTest {

    @Test
    void platformUserEndpointExposesAdminCreateWithoutPublicRegister() throws NoSuchMethodException {
        assertThat(PlatformUserEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/platform/users");
        assertHttpMethod(PlatformUserEndpoint.class, "createUser", POST.class, PlatformUserCreateRequest.class);
        assertHttpMethod(PlatformUserEndpoint.class, "getUser", GET.class, String.class);
        assertThat(Arrays.stream(PlatformUserEndpoint.class.getMethods()).map(Method::getName))
                .doesNotContain("register");
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
                        "status");
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
