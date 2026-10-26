package com.innospots.nexus.console.i18n.endpoint;

import java.lang.reflect.Method;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.entry.ConsolePageRoutes;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;

import static org.assertj.core.api.Assertions.assertThat;

class I18nBundleEndpointContractsTest {

    @Test
    void exposesPublicBundleContractWithoutAuthenticationRequirement() throws NoSuchMethodException {
        assertThat(I18nBundleEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(ConsoleConstant.publicPath("/i18n/bundles"));
        assertThat(I18nBundleEndpoint.class.getAnnotation(Tag.class).name()).isEqualTo("I18n");
        assertThat(I18nBundleEndpoint.class.getAnnotations())
                .noneMatch(annotation -> annotation.annotationType().getSimpleName()
                        .equals("NexusAuthenticatedApi"));

        EndpointModule module = I18nBundleEndpoint.class.getAnnotation(EndpointModule.class);
        assertThat(module).isNotNull();
        assertThat(module.value()).isEqualTo("i18n");
        assertThat(module.domain()).isEqualTo(ConsolePageRoutes.BUILTIN_DOMAIN_KEY);
        assertThat(module.ignoreAuth()).isTrue();
        assertThat(module.ignorePermission()).isTrue();

        Method method = I18nBundleEndpoint.class.getMethod(
                "bundles",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class);
        assertThat(method.getAnnotation(GET.class)).isNotNull();
        assertThat(method.getAnnotation(Operation.class).operationId()).isEqualTo("i18nBundles");

        EndpointGrant grant = method.getAnnotation(EndpointGrant.class);
        assertThat(grant).isNotNull();
        assertThat(grant.action()).isEqualTo(EndpointGrantAction.READ);
        assertThat(grant.name()).isEqualTo("i18n.bundles");
        assertThat(grant.ignoreAuth()).isTrue();
        assertThat(grant.ignorePermission()).isTrue();
        assertThat(grant.auditLog()).isFalse();
    }
}
