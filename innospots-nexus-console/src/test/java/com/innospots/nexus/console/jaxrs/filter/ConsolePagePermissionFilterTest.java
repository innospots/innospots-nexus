package com.innospots.nexus.console.jaxrs.filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;
import com.innospots.nexus.console.permission.authorization.AuthorizationContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationRequest;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsolePagePermissionFilterTest {

    private ConsolePagePermissionAuthorizer pagePermissionAuthorizer;

    private AuthorizationSubjectResolver subjectResolver;

    @BeforeEach
    void setUp() {
        pagePermissionAuthorizer = mock(ConsolePagePermissionAuthorizer.class);
        subjectResolver = mock(AuthorizationSubjectResolver.class);
    }

    @AfterEach
    void tearDown() {
        JaxRsFilterTestSupport.clearThreadState();
    }

    @Test
    void skipsWhenSecurityDisabled() {
        ConsoleWebSecuritySettings security = disabledSecurity();
        ConsolePagePermissionFilter filter = newFilter(security);

        filter.filter(datasourceRequest(null));

        verify(pagePermissionAuthorizer, never()).authorize(any());
    }

    @Test
    void skipsPublicApiPath() {
        ConsolePagePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/public/pages/demo"));

        verify(pagePermissionAuthorizer, never()).authorize(any());
    }

    @Test
    void skipsNonConsolePathPattern() {
        ConsolePagePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status"));

        verify(pagePermissionAuthorizer, never()).authorize(any());
    }

    @Test
    void rejectsMissingPageKeyHeader() {
        ConsolePagePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        assertThatThrownBy(() -> filter.filter(datasourceRequest(null)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void rejectsWhenSubjectMissing() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.empty());
        ConsolePagePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        assertThatThrownBy(() -> filter.filter(datasourceRequest("page-key")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void rejectsWhenAuthorizerDenies() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.of(subject()));
        when(pagePermissionAuthorizer.authorize(any())).thenReturn(AuthorizationDecision.deny("denied"));

        ConsolePagePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        assertThatThrownBy(() -> filter.filter(datasourceRequest("page-key")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void allowsAndStoresAuthorizationContext() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.of(subject()));
        AuthorizationContext context = new AuthorizationContext("ws-1", "page-key", "ds-1", List.of());
        when(pagePermissionAuthorizer.authorize(any())).thenReturn(AuthorizationDecision.allow(context));

        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        ConsolePagePermissionFilter filter = newFilter(security);
        ContainerRequestContext request = datasourceRequest("page-key");

        filter.filter(request);

        assertThat(request.getProperty(RequestProperties.AUTHORIZATION_CONTEXT)).isEqualTo(context);
        ArgumentCaptor<AuthorizationRequest> captor = ArgumentCaptor.forClass(AuthorizationRequest.class);
        verify(pagePermissionAuthorizer).authorize(captor.capture());
        assertThat(captor.getValue().workspaceId()).isEqualTo("ws-1");
        assertThat(captor.getValue().pageKey()).isEqualTo("page-key");
        assertThat(captor.getValue().method()).isEqualTo("GET");
        assertThat(captor.getValue().path()).isEqualTo("/console/datasource/orders");
    }

    private ConsolePagePermissionFilter newFilter(ConsoleWebSecuritySettings security) {
        return new ConsolePagePermissionFilter(security, pagePermissionAuthorizer, subjectResolver);
    }

    private static ConsoleWebSecuritySettings disabledSecurity() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        return security;
    }

    private static ContainerRequestContext datasourceRequest(String pageKey) {
        Map<String, String> headers = pageKey == null
                ? Map.of()
                : Map.of("X-Nexus-Page-Key", pageKey);
        return JaxRsFilterTestSupport.mockRequest("/console/datasource/orders", "GET", headers);
    }

    private static AuthorizationSubject subject() {
        return new AuthorizationSubject("42", Set.of("role-1"), Set.of(), false);
    }
}
