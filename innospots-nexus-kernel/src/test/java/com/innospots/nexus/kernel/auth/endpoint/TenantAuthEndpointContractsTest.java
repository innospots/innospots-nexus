package com.innospots.nexus.kernel.auth.endpoint;

import java.lang.reflect.Method;
import java.util.Arrays;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.kernel.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.kernel.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAuthEndpointContractsTest {

    @Test
    void tenantAuthEndpointAllowsIdentityRegisterAndSelectTenant() throws NoSuchMethodException {
        assertThat(TenantAuthEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/tenant/auth");
        assertHttpMethod(TenantAuthEndpoint.class, "register", POST.class, TenantRegisterRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "login", POST.class, AuthLoginRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "selectTenant", POST.class, SelectTenantRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "refresh", POST.class, TokenRefreshRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "logout", POST.class);
        assertHttpMethod(TenantAuthEndpoint.class, "changePassword", POST.class, PasswordChangeRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "resetPassword", POST.class, PasswordResetRequest.class);
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
