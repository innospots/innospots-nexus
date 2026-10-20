package com.innospots.nexus.console.endpoint.grant.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;

class EndpointGrantAnnotationsTest {

    @Test
    void resolvesGrantDeclaredOnInterfaceMethod() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "createDictionaryType",
                DictionaryTypeCreateRequest.class);

        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, null);

        assertThat(grant).isNotNull();
        assertThat(grant.moduleKey()).isEqualTo("dictionary");
        assertThat(grant.action()).isEqualTo(EndpointGrantAction.CREATE);
        assertThat(grant.name()).isEqualTo("dictionary.type.create");
        assertThat(grant.auditLog()).isTrue();
        assertThat(grant.recordArgs()).isFalse();
    }

    @Test
    void resolveModuleOnContractInterface() {
        assertThat(EndpointGrantAnnotations.resolveModule(DictionaryTypeEndpoint.class).value())
                .isEqualTo("dictionary");
        assertThat(EndpointGrantAnnotations.isGrantResourceType(DictionaryTypeEndpoint.class)).isTrue();
    }

    @Test
    void mergesModuleIgnoreFlags() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "listDictionaryTypeOptions",
                com.innospots.nexus.base.domain.enums.BasicStatus.class);

        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, null);

        assertThat(grant).isNotNull();
        assertThat(grant.ignorePermission()).isTrue();
        assertThat(grant.auditLog()).isFalse();
    }
}
