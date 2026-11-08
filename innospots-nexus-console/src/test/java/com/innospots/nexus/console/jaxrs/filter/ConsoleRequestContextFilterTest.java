package com.innospots.nexus.console.jaxrs.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.jaxrs.support.ConsoleTokenSessionBinder;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ConsoleRequestContextFilter} 线程上下文生命周期测试。
 *
 * <p>重点覆盖异常路径：鉴权/授权过滤器抛出 401/403 时，本过滤器必须已经在请求开始时
 * 清理过残留身份，避免线程池把上一个请求的身份泄漏给后续匿名请求。</p>
 */
class ConsoleRequestContextFilterTest {

    private final ConsoleRequestContextFilter filter = new ConsoleRequestContextFilter();

    @AfterEach
    void tearDown() {
        ConsoleTokenSessionBinder.clear();
    }

    @Test
    void requestPhaseClearsResidualSessionAndSetsTraceId() {
        SessionContext.bindUser(UserSnapshot.simple("stale-user", "stale-user", null));
        TLC.securityRealm("PLATFORM");
        TLC.tenantId("stale-tenant");
        TLC.put(TLC.TRACE_ID, "stale-trace");

        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString(HttpHeaderNames.REQUEST_ID)).thenReturn("rid-current");

        filter.filter(request);

        assertThat(SessionContext.user()).isEmpty();
        assertThat(TLC.securityRealm()).isNull();
        assertThat(TLC.tenantId()).isNull();
        assertThat(TLC.getString(TLC.TRACE_ID)).isEqualTo("rid-current");
    }

    @Test
    void responsePhaseClearsSessionAndWritesRequestIdHeader() {
        SessionContext.bindUser(UserSnapshot.simple("u-1", "u-1", null));
        TLC.securityRealm("TENANT");

        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getProperty(RequestProperties.REQUEST_ID)).thenReturn("rid-2");
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        when(response.getHeaders()).thenReturn(headers);

        filter.filter(request, response);

        assertThat(headers.getFirst(HttpHeaderNames.REQUEST_ID)).isEqualTo("rid-2");
        assertThat(SessionContext.user()).isEmpty();
        assertThat(TLC.securityRealm()).isNull();
    }

    @Test
    void runsBeforeAuthenticationFilters() {
        jakarta.annotation.Priority priority =
                ConsoleRequestContextFilter.class.getAnnotation(jakarta.annotation.Priority.class);
        assertThat(priority).isNotNull();
        assertThat(priority.value())
                .isLessThan(jakarta.ws.rs.Priorities.AUTHENTICATION)
                .isLessThan(ConsoleAuthenticationFilter.class
                        .getAnnotation(jakarta.annotation.Priority.class).value());
    }
}
