package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicPasswordResetEndpointContractsTest {

    @Test
    void passwordResetEndpointUsesPublicPasswordPath() throws NoSuchMethodException {
        assertThat(PlatformPublicPasswordResetEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH);
        assertThat(PlatformPublicPasswordResetEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicPasswordReset");
        assertThat(PlatformPublicPasswordResetEndpoint.class.getMethod("resetPassword", PasswordResetRequest.class)
                        .getAnnotation(Operation.class)
                        .operationId())
                .isEqualTo("platformAuthPasswordReset");
        assertThat(PlatformPublicPasswordResetEndpoint.class.getMethod("resetPassword", PasswordResetRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
    }
}
