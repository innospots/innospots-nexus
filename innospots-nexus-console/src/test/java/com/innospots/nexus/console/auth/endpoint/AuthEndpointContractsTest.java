package com.innospots.nexus.console.auth.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.console.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEndpointContractsTest {

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

    @Test
    void tenantAuthEndpointAllowsIdentityRegisterAndSelectTenant() throws NoSuchMethodException {
        assertThat(TenantAuthEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/tenant/auth");
        assertHttpMethod(TenantAuthEndpoint.class, "register", POST.class, TenantRegisterRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "login", POST.class, AuthLoginRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "selectTenant", POST.class, SelectTenantRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "refresh", POST.class, TokenRefreshRequest.class);
        assertHttpMethod(TenantAuthEndpoint.class, "logout", POST.class);
    }

    @Test
    void authRequestsAndTokenAreRecords() {
        assertRecordComponents(AuthLoginRequest.class, "login", "encryptedPassword");
        assertRecordComponents(TenantRegisterRequest.class,
                "userName", "displayName", "email", "mobile", "region", "timeZone", "language", "encryptedPassword");
        assertRecordComponents(SelectTenantRequest.class, "tenantId");
        assertRecordComponents(AuthTokenVo.class,
                "realm", "tokenType", "accessToken", "refreshToken", "tenantId", "tenantMemberId");
        assertThat(SecurityRealm.values()).containsExactly(SecurityRealm.PLATFORM, SecurityRealm.TENANT);
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
        assertThat(Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly(names);
    }
}
