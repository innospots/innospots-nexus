package com.innospots.nexus.spring.console.jaxrs;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.jaxrs.filter.ConsoleAuditLogFilter;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


class JerseyEndpointMethodResolverTest {

    private final AtomicReference<InvocationLogContext> recorded = new AtomicReference<>();
    private final ConsoleAuditLogFilter filter = new ConsoleAuditLogFilter(
            new AuditLogRecorder(recorded::set), new JerseyEndpointMethodResolver());

    @AfterEach
    void clearSession() {
        TLC.clear();
    }

    @Test
    void returnsNoResourceForNonJerseyRequestContext() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getUriInfo()).thenReturn(mock(UriInfo.class));

        assertThat(new JerseyEndpointMethodResolver().resolve(request)).isNull();
    }

    @Test
    void recordsInterfaceAnnotationUsingJerseyMatchedImplementationMethod() {
        ContainerRequest request = request("create");
        SessionContext.bindUser(UserSnapshot.simple(1L, "alice", null));

        filter.filter(request);
        filter.filter(request, response(200));

        InvocationLogContext context = recorded.get();
        assertThat(context).isNotNull();
        assertThat(context.action()).isEqualTo("audit.create");
        assertThat(context.methodName()).isEqualTo("create");
        assertThat(context.className()).isEqualTo(AuditResource.class.getName());
        assertThat(context.httpPath()).isEqualTo("POST /audit");
        assertThat(context.actor()).isEqualTo("alice");
        assertThat(context.elapsedMillis()).isGreaterThanOrEqualTo(0);
        assertThat(context.success()).isTrue();
        assertThat(request.getPropertyNames()).isEmpty();
    }

    @Test
    void recordsMappedFailure() {
        ContainerRequest request = request("create");
        NexusException failure = NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        filter.filter(request);
        request.setProperty(RequestProperties.FAILURE_CAUSE, failure);

        filter.filter(request, response(400));

        assertThat(recorded.get().success()).isFalse();
        assertThat(recorded.get().exception()).isSameAs(failure);
    }

    @Test
    void skipsUnannotatedMethodAndClearsRequestState() {
        ContainerRequest request = request("read");

        filter.filter(request);
        filter.filter(request, response(200));

        assertThat(recorded.get()).isNull();
        assertThat(request.getPropertyNames()).isEmpty();
    }

    @Test
    void skipsUnmatchedRequestAndClearsRequestState() {
        ContainerRequest request = request(null);

        filter.filter(request);
        filter.filter(request, response(404));

        assertThat(recorded.get()).isNull();
        assertThat(request.getPropertyNames()).isEmpty();
    }

    private ContainerRequest request(String methodName) {
        ContainerRequest request = spy(new ContainerRequest(
                URI.create("http://localhost/"), URI.create("http://localhost/audit"),
                "POST", null, new MapPropertiesDelegate()));
        ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        doReturn(uriInfo).when(request).getUriInfo();
        when(uriInfo.getPath()).thenReturn("audit");
        if (methodName != null) {
            when(uriInfo.getMatchedResourceMethod()).thenReturn(Resource.builder(AuditResource.class)
                    .build().getResourceMethods().stream()
                    .filter(method -> methodName.equals(method.getInvocable().getHandlingMethod().getName()))
                    .findFirst().orElseThrow());
        }
        return request;
    }

    private ContainerResponseContext response(int status) {
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        when(response.getStatus()).thenReturn(status);
        return response;
    }

    private interface AuditedContract {

        @EndpointGrant(action = EndpointGrantAction.CREATE, name = "audit.create", recordArgs = false, auditLog = true)
        String create();
    }

    /**
     * 仅接口声明审计注解的 Jersey 测试资源。
     *
     * @author Smars
     * @date 2026/10/17
     * @see ConsoleAuditLogFilter
     */
    @Path("/audit")
    public static class AuditResource implements AuditedContract {

        @Override
        @POST
        public String create() {
            return "created";
        }

        /**
         * 返回无需审计的只读结果。
         *
         * @return 测试响应
         */
        @GET
        public String read() {
            return "read";
        }
    }
}
