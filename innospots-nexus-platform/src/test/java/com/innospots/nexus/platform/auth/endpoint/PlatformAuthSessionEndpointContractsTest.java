package com.innospots.nexus.platform.auth.endpoint;

import java.util.Arrays;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAuthSessionEndpointContractsTest {

    @Test
    void sessionEndpointUsesAuthenticatedAuthPathAndHasNoRegister() {
        assertThat(PlatformAuthSessionEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.AUTH_PATH);
        assertThat(PlatformAuthSessionEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformAuthSession");
        assertThat(Arrays.stream(PlatformAuthSessionEndpoint.class.getMethods()).map(java.lang.reflect.Method::getName))
                .doesNotContain("register");
    }

    @Test
    void sessionEndpointExposesRefreshLogoutAndChangePassword() throws NoSuchMethodException {
        assertThat(PlatformAuthSessionEndpoint.class.getMethod("refresh", TokenRefreshRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
        assertThat(PlatformConstant.AUTH_REFRESH_PATH).isEqualTo("/api/platform/auth/refresh");
        assertThat(PlatformAuthSessionEndpoint.class.getMethod("logout").getAnnotation(POST.class))
                .isNotNull();
        assertThat(PlatformAuthSessionEndpoint.class.getMethod("changePassword", PasswordChangeRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
    }
}
