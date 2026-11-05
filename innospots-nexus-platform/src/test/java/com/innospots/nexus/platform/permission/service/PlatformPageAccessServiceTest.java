package com.innospots.nexus.platform.permission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.scope.service.SessionScopeTlc;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.platform.permission.domain.vo.ActionAccessVo;

class PlatformPageAccessServiceTest {

    @Test
    void requiresSessionForAuthenticatedPage() {
        PlatformActionAccessService actions = mock(PlatformActionAccessService.class);
        PlatformPageAccessService service = new PlatformPageAccessService(mock(PageAccessVerifier.class),
                Optional::empty, actions);
        var result = service.verify(new PageAccessVerifyRequest("", "", "password-change", "view",
                "authenticated", List.of()));
        assertThat(result.allowed()).isFalse();
        assertThat(result.reason()).isEqualTo("Authentication is required");
    }

    @Test
    void includesActionResultsForAuthenticatedPage() {
        PlatformActionAccessService actions = mock(PlatformActionAccessService.class);
        when(actions.verify(any())).thenReturn(new ActionAccessVo(Map.of("POST:/api/test", false), List.of()));
        AuthorizationSubject subject = new AuthorizationSubject("user", Set.of(), Set.of(), false);
        AuthorizationSubjectResolver subjects = () -> Optional.of(subject);
        PlatformPageAccessService service = new PlatformPageAccessService(mock(PageAccessVerifier.class),
                subjects, actions);
        var result = service.verify(new PageAccessVerifyRequest("", "", "password-change", "view",
                "authenticated", List.of(new PageAccessVerifyRequest.RequestItem("POST", "/api/test"))));
        assertThat(result.allowed()).isTrue();
        assertThat(result.requestPermissions()).containsEntry("POST:/api/test", false);
    }

    @Test
    void rejectsInvalidModeAndUnregisteredPageGrant() {
        PlatformActionAccessService actions = mock(PlatformActionAccessService.class);
        PlatformPageAccessService service = new PlatformPageAccessService(mock(PageAccessVerifier.class),
                Optional::empty, actions);
        assertThat(service.verify(new PageAccessVerifyRequest("", "", "x", "view", "public", List.of()))
                .allowed()).isFalse();
        assertThat(service.verify(new PageAccessVerifyRequest("missing", "module", "x", "view", "grant", List.of()))
                .reason()).isEqualTo("Invalid page grant configuration");
    }

    @Test
    void grantPageUsesCatalogAndRoleDecisionBeforeReturningActions() {
        PlatformActionAccessService actions = mock(PlatformActionAccessService.class);
        PageAccessVerifier verifier = mock(PageAccessVerifier.class);
        when(actions.hasPageGrant("operation", "role", "platform-role-main", "view")).thenReturn(true);
        when(actions.verify(any())).thenReturn(new ActionAccessVo(Map.of("POST:/api/roles", false), List.of("view")));
        AuthorizationSubject subject = new AuthorizationSubject("user", Set.of("role"), Set.of(), false);
        PlatformPageAccessService service = new PlatformPageAccessService(verifier, () -> Optional.of(subject), actions);
        PageAccessVerifyRequest request = new PageAccessVerifyRequest("operation", "role", "platform-role-main",
                "view", "grant", List.of(new PageAccessVerifyRequest.RequestItem("POST", "/api/roles")));
        try {
            TLC.securityRealm("PLATFORM");
            ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
            when(verifier.verify(request, null, subject)).thenReturn(PageAccessVerifyResult.allow());
            var allowed = service.verify(request);
            assertThat(allowed.allowed()).isTrue();
            assertThat(allowed.requestPermissions()).containsEntry("POST:/api/roles", false);
            assertThat(allowed.grantGroups()).containsExactly("view");
            when(verifier.verify(request, null, subject)).thenReturn(PageAccessVerifyResult.deny("Page permission is required"));
            assertThat(service.verify(request).allowed()).isFalse();
        } finally {
            SessionScopeTlc.clear();
        }
    }
}
