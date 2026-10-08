package com.innospots.nexus.console.jaxrs.filter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.jaxrs.support.ConsoleTokenSessionBinder;
import com.innospots.nexus.core.jaxrs.support.RequestScope;

import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class JaxRsFilterTestSupport {

    private JaxRsFilterTestSupport() {
    }

    static void clearThreadState() {
        ConsoleTokenSessionBinder.clear();
        RequestScope.clear();
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.clear();
    }

    static void bindWorkspace(String tenantId, String workspaceId) {
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                tenantId,
                workspaceId,
                workspaceId,
                workspaceId,
                BasicStatus.ENABLED));
    }

    static TokenClaims validAccessClaims() {
        return new TokenClaims(
                SecurityRealm.PLATFORM,
                TokenIssuer.PURPOSE_ACCESS,
                "IDENTITY",
                "42",
                "tenant-1",
                null,
                "ws-1",
                null,
                Instant.now().getEpochSecond() + 3600);
    }

    static ContainerRequestContext mockRequest(String path, String method, Map<String, String> headers) {
        Map<String, String> headerMap = headers == null ? Map.of() : new HashMap<>(headers);
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn(path);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getMethod()).thenReturn(method);
        when(requestContext.getHeaderString(anyString())).thenAnswer((Answer<String>) invocation -> {
            String name = invocation.getArgument(0);
            return headerMap.get(name);
        });
        Map<String, Object> properties = new ConcurrentHashMap<>();
        doAnswer(invocation -> {
            properties.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(requestContext).setProperty(anyString(), any());
        when(requestContext.getProperty(anyString())).thenAnswer(invocation -> properties.get(invocation.getArgument(0)));
        return requestContext;
    }

    static ContainerRequestContext mockRequest(String path) {
        return mockRequest(path, "GET", Map.of());
    }

    static ContainerResponseContext mockResponse() {
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        return responseContext;
    }
}
