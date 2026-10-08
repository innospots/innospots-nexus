package com.innospots.nexus.console.jaxrs.filter;

import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;
import com.innospots.nexus.core.jaxrs.support.RequestScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleRequestContextFilterTest {

    private final ConsoleRequestContextFilter filter = new ConsoleRequestContextFilter();

    @AfterEach
    void tearDown() {
        JaxRsFilterTestSupport.clearThreadState();
    }

    @Test
    void generatesRequestIdWhenHeaderMissing() {
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status");

        filter.filter(request);

        Object requestId = request.getProperty(RequestProperties.REQUEST_ID);
        assertThat(requestId).isNotNull();
        assertThat(String.valueOf(requestId)).hasSize(32);
        assertThat(TLC.getString(TLC.TRACE_ID)).isEqualTo(String.valueOf(requestId));
        assertThat(RequestScope.current()).isSameAs(request);
    }

    @Test
    void usesRequestIdFromHeader() {
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                Map.of(HttpHeaderNames.REQUEST_ID, "trace-from-client"));

        filter.filter(request);

        assertThat(request.getProperty(RequestProperties.REQUEST_ID)).isEqualTo("trace-from-client");
        assertThat(TLC.getString(TLC.TRACE_ID)).isEqualTo("trace-from-client");
    }

    @Test
    void responseFilterEchoesRequestIdAndClearsThreadState() {
        SessionContext.bindUser(UserSnapshot.simple(1L, "u", null));
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                Map.of(HttpHeaderNames.REQUEST_ID, "rid-abc"));
        filter.filter(request);

        ContainerResponseContext response = JaxRsFilterTestSupport.mockResponse();
        filter.filter(request, response);

        assertThat(response.getHeaders().getFirst(HttpHeaderNames.REQUEST_ID)).isEqualTo("rid-abc");
        assertThat(SessionContext.user()).isEmpty();
        assertThat(RequestScope.current()).isNull();
        assertThat(TLC.getString(TLC.TRACE_ID)).isNull();
    }
}
