package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicAuthEndpointContractsTest {

    @Test
    void publicAuthEndpointUsesPublicPathAndHasLoginCaptcha() throws NoSuchMethodException {
        assertThat(PlatformPublicAuthEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_AUTH_PATH);
        assertThat(PlatformPublicAuthEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicAuth");
        assertThat(PlatformPublicAuthEndpoint.class.getMethod("login", AuthLoginRequest.class)
                        .getAnnotation(Operation.class)
                        .operationId())
                .isEqualTo("platformAuthLogin");
        assertThat(PlatformPublicAuthEndpoint.class.getMethod("login", AuthLoginRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
    }
}
