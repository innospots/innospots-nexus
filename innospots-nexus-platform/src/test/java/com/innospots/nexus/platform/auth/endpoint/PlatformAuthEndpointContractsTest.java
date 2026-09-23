package com.innospots.nexus.platform.auth.endpoint;

import java.lang.reflect.Method;
import java.util.Arrays;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAuthEndpointContractsTest {

    @Test
    void platformAuthEndpointHasNoPublicRegister() throws NoSuchMethodException {
        assertThat(PlatformAuthEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/platform/auth");
        assertHttpMethod(PlatformAuthEndpoint.class, "login", POST.class, AuthLoginRequest.class);
        assertHttpMethod(PlatformAuthEndpoint.class, "refresh", POST.class, TokenRefreshRequest.class);
        assertHttpMethod(PlatformAuthEndpoint.class, "logout", POST.class);
        assertHttpMethod(PlatformAuthEndpoint.class, "changePassword", POST.class, PasswordChangeRequest.class);
        assertHttpMethod(PlatformAuthEndpoint.class, "resetPassword", POST.class, PasswordResetRequest.class);
        assertThat(Arrays.stream(PlatformAuthEndpoint.class.getMethods()).map(Method::getName))
                .doesNotContain("register");
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
