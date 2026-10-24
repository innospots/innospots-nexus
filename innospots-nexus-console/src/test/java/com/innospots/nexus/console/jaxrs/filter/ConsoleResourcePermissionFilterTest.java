package com.innospots.nexus.console.jaxrs.filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestProperties;
import com.innospots.nexus.console.jaxrs.support.ResolvedEndpointBinding;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.console.logger.EndpointGrantFixtures;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;
import com.innospots.nexus.console.permission.authorization.AuthorizationContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleResourcePermissionFilterTest {

    private ConsoleResourcePermissionAuthorizer resourcePermissionAuthorizer;

    private AuthorizationSubjectResolver subjectResolver;

    @BeforeEach
    void setUp() {
        resourcePermissionAuthorizer = mock(ConsoleResourcePermissionAuthorizer.class);
        subjectResolver = mock(AuthorizationSubjectResolver.class);
    }

    @AfterEach
    void tearDown() {
        JaxRsFilterTestSupport.clearThreadState();
    }

    @Test
    void skipsWhenSecurityDisabled() {
        ConsoleWebSecuritySettings security = disabledSecurity();
        ConsoleResourcePermissionFilter filter = newFilter(security);

        filter.filter(dictionaryRequest(EndpointGrantFixtures.dictionaryTypeCreateGrant()));

        verify(resourcePermissionAuthorizer, never()).authorizeEndpoint(any(), any(), any());
    }

    @Test
    void skipsPublicApiPath() {
        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/public/page-access/verify"));

        verify(resourcePermissionAuthorizer, never()).authorizeEndpoint(any(), any(), any());
    }

    @Test
    void skipsNonConsolePathPattern() {
        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/console/datasource/orders");
        request.setProperty(
                ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING,
                new ResolvedEndpointBinding(
                        EndpointGrantFixtures.dictionaryTypeCreateGrant(), "Test", "test"));

        filter.filter(request);

        verify(resourcePermissionAuthorizer, never()).authorizeEndpoint(any(), any(), any());
    }

    @Test
    void skipsWhenEndpointGrantMissing() {
        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/d/platform/dictionary-types"));

        verify(resourcePermissionAuthorizer, never()).authorizeEndpoint(any(), any(), any());
    }

    @Test
    void skipsWhenEndpointGrantIgnoresPermission() {
        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());
        ContainerRequestContext request = dictionaryRequest(grantIgnoringPermission());

        filter.filter(request);

        verify(resourcePermissionAuthorizer, never()).authorizeEndpoint(any(), any(), any());
    }

    @Test
    void rejectsWhenSubjectMissing() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.empty());
        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        assertThatThrownBy(() -> filter.filter(
                dictionaryRequest(EndpointGrantFixtures.dictionaryTypeCreateGrant())))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void rejectsWhenAuthorizerDenies() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.of(subject()));
        when(resourcePermissionAuthorizer.authorizeEndpoint(any(), any(), any()))
                .thenReturn(AuthorizationDecision.deny("denied"));

        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());

        assertThatThrownBy(() -> filter.filter(
                dictionaryRequest(EndpointGrantFixtures.dictionaryTypeCreateGrant())))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
    }

    @Test
    void allowsAndStoresAuthorizationContext() {
        JaxRsFilterTestSupport.bindWorkspace("tenant-1", "ws-1");
        when(subjectResolver.resolve()).thenReturn(Optional.of(subject()));
        AuthorizationContext context = new AuthorizationContext(
                "ws-1",
                "dictionary.type.create:platform.dictionary",
                null,
                List.of());
        when(resourcePermissionAuthorizer.authorizeEndpoint(any(), any(), any()))
                .thenReturn(AuthorizationDecision.allow(context));

        ConsoleResourcePermissionFilter filter = newFilter(new ConsoleWebSecuritySettings());
        ResolvedEndpointGrant grant = EndpointGrantFixtures.dictionaryTypeCreateGrant();
        ContainerRequestContext request = dictionaryRequest(grant);

        filter.filter(request);

        assertThat(request.getProperty(RequestProperties.AUTHORIZATION_CONTEXT)).isEqualTo(context);
        ArgumentCaptor<ResolvedEndpointGrant> grantCaptor = ArgumentCaptor.forClass(ResolvedEndpointGrant.class);
        verify(resourcePermissionAuthorizer).authorizeEndpoint(
                eq("ws-1"), grantCaptor.capture(), eq(subject()));
        assertThat(grantCaptor.getValue().name()).isEqualTo("dictionary.type.create");
        assertThat(grantCaptor.getValue().moduleKey()).isEqualTo("dictionary");
    }

    private ConsoleResourcePermissionFilter newFilter(ConsoleWebSecuritySettings security) {
        return new ConsoleResourcePermissionFilter(security, resourcePermissionAuthorizer, subjectResolver);
    }

    private static ConsoleWebSecuritySettings disabledSecurity() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        return security;
    }

    private static ContainerRequestContext dictionaryRequest(ResolvedEndpointGrant grant) {
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/platform/dictionary-types",
                "POST",
                Map.of());
        request.setProperty(
                ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING,
                new ResolvedEndpointBinding(grant, "DictionaryTypeEndpoint", "createDictionaryType"));
        return request;
    }

    private static AuthorizationSubject subject() {
        return new AuthorizationSubject("42", Set.of("role-1"), Set.of(), false);
    }

    private static ResolvedEndpointGrant grantIgnoringPermission() {
        ResolvedEndpointGrant base = EndpointGrantFixtures.dictionaryTypeCreateGrant();
        return new ResolvedEndpointGrant(
                base.moduleKey(),
                base.action(),
                base.name(),
                base.label(),
                base.recordArgs(),
                base.auditLog(),
                true,
                false);
    }
}
