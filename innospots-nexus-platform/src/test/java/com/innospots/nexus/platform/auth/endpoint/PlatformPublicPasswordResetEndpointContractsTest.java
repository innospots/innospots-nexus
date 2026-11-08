package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.endpoint.PublicPasswordResetEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicPasswordResetEndpointContractsTest {

    @Test
    void implementsConsolePublicPasswordResetEndpointOnPlatformPath() throws NoSuchMethodException {
        assertThat(PublicPasswordResetEndpoint.class.isAssignableFrom(PlatformPublicPasswordResetEndpoint.class))
                .isTrue();
        assertThat(PlatformPublicPasswordResetEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH);
        assertThat(PlatformPublicPasswordResetEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicPasswordReset");
        assertThat(PublicPasswordResetEndpoint.class.getMethod("resetPassword", PasswordResetRequest.class)
                        .getAnnotation(Operation.class)
                        .operationId())
                .isEqualTo("authPasswordReset");
        /*
         * 实现类只写 @Override，不重复接口注解 —— 仓库统一约定
         * （见 PlatformStandaloneEndpointGrantContractsTest
         * #consoleContractImplementationsDoNotRepeatInterfaceMethodAnnotations）。
         * 因此实现类方法上不声明 @Operation / @POST。
         */
        assertThat(PublicPasswordResetEndpoint.class.getMethod("resetPassword", PasswordResetRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
    }
}
