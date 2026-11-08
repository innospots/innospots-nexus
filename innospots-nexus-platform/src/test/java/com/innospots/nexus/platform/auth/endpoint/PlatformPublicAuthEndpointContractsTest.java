package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.endpoint.PublicAuthEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPublicAuthEndpointContractsTest {

    @Test
    void implementsConsolePublicAuthEndpointOnPlatformPath() {
        assertThat(PublicAuthEndpoint.class.isAssignableFrom(PlatformPublicAuthEndpoint.class)).isTrue();
        assertThat(PlatformPublicAuthEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_AUTH_PATH);
    }

    @Test
    void publicAuthContractUsesConsoleOpenApiAndHttpVerbs() throws NoSuchMethodException {
        assertThat(PlatformPublicAuthEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.PUBLIC_AUTH_PATH);
        assertThat(PlatformPublicAuthEndpoint.class.getAnnotation(Tag.class).name())
                .isEqualTo("PlatformPublicAuth");
        assertThat(PublicAuthEndpoint.class.getMethod("login", AuthLoginRequest.class)
                        .getAnnotation(Operation.class)
                        .operationId())
                .isEqualTo("authLogin");
        /*
         * 实现类只写 @Override，不重复接口注解 —— 这是仓库统一约定
         * （见 PlatformStandaloneEndpointGrantContractsTest
         * #consoleContractImplementationsDoNotRepeatInterfaceMethodAnnotations）。
         * 因此这里不再断言实现类方法上的 @Operation；HTTP 路由与 operationId
         * 统一由 console 契约接口承载。
         */
        assertThat(PublicAuthEndpoint.class.getMethod("login", AuthLoginRequest.class)
                        .getAnnotation(POST.class))
                .isNotNull();
        assertThat(PublicAuthEndpoint.class.getMethod("getPasswordEncryptionPublicKey")
                        .getAnnotation(Operation.class)
                        .operationId())
                .isEqualTo("authRsaPublicKey");
        assertThat(PublicAuthEndpoint.class.getMethod("getPasswordEncryptionPublicKey")
                        .getAnnotation(GET.class))
                .isNotNull();
    }
}
