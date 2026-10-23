package com.innospots.nexus.console.jaxrs.filter;

import java.lang.reflect.Method;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestGrants;
import com.innospots.nexus.console.jaxrs.support.ResolvedEndpointBinding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleResolvedEndpointGrantFilterTest {

    @Test
    void storesResolvedGrantOnRequestContext() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "createDictionaryType", DictionaryTypeCreateRequest.class);
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        doReturn(DictionaryTypeEndpoint.class).when(resourceInfo).getResourceClass();
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/dictionary-types", "POST", null);

        ConsoleResolvedEndpointGrantFilter filter =
                new ConsoleResolvedEndpointGrantFilter(context -> resourceInfo);
        filter.filter(request);

        ResolvedEndpointGrant grant = ConsoleRequestGrants.resolvedGrant(request);
        assertThat(grant).isNotNull();
        assertThat(grant.name()).isEqualTo("dictionary.type.create");
        ResolvedEndpointBinding binding = ConsoleRequestGrants.binding(request);
        assertThat(binding).isNotNull();
        assertThat(binding.grant()).isEqualTo(grant);
        assertThat(binding.resourceClassName()).isEqualTo(DictionaryTypeEndpoint.class.getName());
        assertThat(binding.resourceMethodName()).isEqualTo("createDictionaryType");
    }

    @Test
    void skipsWhenResourceNotResolved() {
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/unknown");
        ConsoleResolvedEndpointGrantFilter filter =
                new ConsoleResolvedEndpointGrantFilter(context -> null);

        filter.filter(request);

        assertThat(ConsoleRequestGrants.resolvedGrant(request)).isNull();
    }
}
