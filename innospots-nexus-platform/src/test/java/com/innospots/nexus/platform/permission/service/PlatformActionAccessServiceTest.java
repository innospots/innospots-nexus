package com.innospots.nexus.platform.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.authorization.AuthorizationContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.domain.session.ConsoleSessionContext;
import com.innospots.nexus.console.scope.domain.session.ConsoleSessionScope;
import com.innospots.nexus.console.scope.domain.session.SessionScopeTlc;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessItem;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessRequest;
import com.innospots.nexus.platform.settings.endpoint.PlatformConsoleSiteNameSettingEndpoint;

class PlatformActionAccessServiceTest {

    @Test
    void deniesUnknownMalformedAndAmbiguousPaths() {
        EndpointGrantManager manager = new EndpointGrantManager();
        manager.registerResource(Fixture.class);
        PlatformActionAccessService service = new PlatformActionAccessService(manager,
                mock(ConsoleResourcePermissionAuthorizer.class), Optional::empty,
                mock(PermissionVisibilityService.class), mock(ConsoleCatalogService.class));
        var result = service.verify(new ActionAccessRequest("operation", "fixture", List.of(
                new ActionAccessItem("GET", "/api/fixture/public"),
                new ActionAccessItem("GET", "/api/fixture/${row.id}"),
                new ActionAccessItem("GET", "/api/fixture/${row.id}/detail"),
                new ActionAccessItem("GET", "/api/unknown"),
                new ActionAccessItem("GET", "/api/fixture/prefix${row.id}"))));
        assertThat(result.allowed()).containsEntry("GET:/api/fixture/public", false)
                .containsEntry("GET:/api/fixture/${row.id}", false)
                .containsEntry("GET:/api/fixture/${row.id}/detail", true)
                .containsEntry("GET:/api/unknown", false)
                .containsEntry("GET:/api/fixture/prefix${row.id}", false);
        assertThat(result.grantGroups()).isEmpty();
    }

    @Test
    void forwardsSettingsUpdateCatalogGroupForNonAdministrator() {
        EndpointGrantManager manager = new EndpointGrantManager();
        manager.registerResource(PlatformConsoleSiteNameSettingEndpoint.class);
        ConsoleResourcePermissionAuthorizer authorizer = mock(ConsoleResourcePermissionAuthorizer.class);
        PermissionVisibilityService visibility = mock(PermissionVisibilityService.class);
        ConsoleCatalogService catalog = mock(ConsoleCatalogService.class);
        AuthorizationSubject subject = new AuthorizationSubject("user", Set.of("role"), Set.of(), false);
        when(visibility.grantedGrantKeys(any(), any())).thenReturn(Set.of());
        when(catalog.permissionCatalog()).thenReturn(List.of());
        when(authorizer.authorizeEndpoint(isNull(), any(), any())).thenReturn(AuthorizationDecision.allow(
                new AuthorizationContext(null, "system.settings:update", null, List.of())));
        PlatformActionAccessService service = new PlatformActionAccessService(
                manager, authorizer, () -> Optional.of(subject), visibility, catalog);

        try {
            TLC.securityRealm("PLATFORM");
            ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
            var result = service.verify(new ActionAccessRequest("system", "settings", List.of(
                    new ActionAccessItem("PUT", "/api/d/platform/settings/console-site-name"))));

            ArgumentCaptor<ResolvedEndpointGrant> grantCaptor =
                    ArgumentCaptor.forClass(ResolvedEndpointGrant.class);
            verify(authorizer).authorizeEndpoint(isNull(), grantCaptor.capture(), any());
            assertThat(result.allowed())
                    .containsEntry("PUT:/api/d/platform/settings/console-site-name", true);
            assertThat(grantCaptor.getValue().action()).isEqualTo(EndpointGrantAction.UPDATE);
            assertThat(grantCaptor.getValue().grantGroup()).isEqualTo("update");
        } finally {
            SessionScopeTlc.clear();
        }
    }

    @Path("/api/fixture")
    @EndpointModule(value = "fixture", domain = "operation")
    public static class Fixture {
        @GET
        @Path("public")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture.public", ignoreAuth = true,
                ignorePermission = true)
        public void publicRoute() { }

        @GET
        @Path("{id}")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture.read", ignoreAuth = true,
                ignorePermission = true)
        public void read() { }

        @GET
        @Path("{id}/detail")
        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture.detail", ignoreAuth = true,
                ignorePermission = true)
        public void detail() { }
    }
}
