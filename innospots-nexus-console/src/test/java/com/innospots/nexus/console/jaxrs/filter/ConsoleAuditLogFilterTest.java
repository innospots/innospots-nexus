package com.innospots.nexus.console.jaxrs.filter;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleAuditLogFilterTest {

    @Test
    void recordsEndpointFromHostResolverWithoutJerseyRequestContext() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "createDictionaryType", DictionaryTypeCreateRequest.class);
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        doReturn(DictionaryTypeEndpoint.class).when(resourceInfo).getResourceClass();
        ContainerRequestContext request =
                JaxRsFilterTestSupport.mockRequest("dictionary-types", "POST", null);
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        when(response.getStatus()).thenReturn(200);
        AtomicReference<InvocationLogContext> recorded = new AtomicReference<>();
        ConsoleResolvedEndpointGrantFilter grantFilter =
                new ConsoleResolvedEndpointGrantFilter(context -> resourceInfo);
        ConsoleAuditLogFilter filter = new ConsoleAuditLogFilter(new AuditLogRecorder(recorded::set));

        grantFilter.filter(request);
        filter.filter(request);
        filter.filter(request, response);

        assertThat(recorded.get()).isNotNull();
        assertThat(recorded.get().action()).isEqualTo("CREATE");
        assertThat(recorded.get().target()).isEqualTo("dictionary.type.create");
        assertThat(recorded.get().methodName()).isEqualTo("createDictionaryType");
        assertThat(recorded.get().httpPath()).isEqualTo("POST /dictionary-types");
        assertThat(recorded.get().success()).isTrue();
    }
}
