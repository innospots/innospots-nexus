package com.innospots.nexus.console.permission.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.logger.EndpointGrantFixtures;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;

class ConsoleResourcePermissionAuthorizerTest {

    @Test
    void allowsAdministratorForResourceKey() {
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                mock(ConsoleCatalogResourceDao.class), mock(PermissionGrantDao.class));

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                List.of("platform.dictionary:dictionary.type.create"),
                new AuthorizationSubject("u1", Set.of(), Set.of(), true)));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.context().resourceKey())
                .isEqualTo("platform.dictionary:dictionary.type.create");
    }

    @Test
    void allowsSubjectWithMatchingGrant() {
        PermissionGrantDao grantDao = grantDaoForKey("platform.dictionary:dictionary.type.create", true);
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                mock(ConsoleCatalogResourceDao.class), grantDao);

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                List.of("platform.dictionary:dictionary.type.create"),
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false)));

        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void deniesSubjectWithoutMatchingGrant() {
        PermissionGrantDao grantDao = grantDaoForKey("platform.dictionary:dictionary.type.create", false);
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                mock(ConsoleCatalogResourceDao.class), grantDao);

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                List.of("platform.dictionary:dictionary.type.create"),
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false)));

        assertThat(decision.allowed()).isFalse();
    }

    @Test
    void allowsWhenAnyCandidateKeyMatchesGrant() {
        PermissionGrantDao grantDao = grantDaoForKey("platform.dictionary:default", true);
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                mock(ConsoleCatalogResourceDao.class), grantDao);

        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "ws-1",
                List.of(
                        "platform.dictionary:dictionary.type.create",
                        "platform.dictionary:default"),
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false)));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.context().resourceKey()).isEqualTo("platform.dictionary:default");
    }

    @Test
    void authorizeEndpointBuildsResourceKeyFromGrantAndModule() {
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(dictionaryModule()));
        PermissionGrantDao grantDao = grantDaoForKey("platform.dictionary:dictionary.type.create", true);
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                resourceDao, grantDao);
        ResolvedEndpointGrant grant = EndpointGrantFixtures.dictionaryTypeCreateGrant();

        AuthorizationDecision decision = authorizer.authorizeEndpoint(
                "ws-1",
                grant,
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.context().resourceKey())
                .isEqualTo("platform.dictionary:dictionary.type.create");
    }

    @Test
    void authorizeEndpointAllowsWhenOnlyGroupGrantMatches() {
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(dictionaryModule()));
        PermissionGrantDao grantDao = grantDaoForKey("platform.dictionary:default", true);
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                resourceDao, grantDao);
        ResolvedEndpointGrant grant = new ResolvedEndpointGrant(
                "dictionary",
                "platform",
                ResolvedEndpointGrant.DEFAULT_GRANT_GROUP,
                EndpointGrantAction.READ,
                "dictionary.type.page",
                emptyLabel(),
                false,
                false,
                false,
                false);

        AuthorizationDecision decision = authorizer.authorizeEndpoint(
                "ws-1",
                grant,
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.context().resourceKey()).isEqualTo("platform.dictionary:default");
    }

    @Test
    void authorizeEndpointDeniesWhenModuleMissing() {
        ConsoleCatalogResourceDao resourceDao = mock(ConsoleCatalogResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of());
        ConsoleResourcePermissionAuthorizer authorizer = new ConsoleResourcePermissionAuthorizer(
                resourceDao, mock(PermissionGrantDao.class));

        AuthorizationDecision decision = authorizer.authorizeEndpoint(
                "ws-1",
                EndpointGrantFixtures.dictionaryTypeCreateGrant(),
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false));

        assertThat(decision.allowed()).isFalse();
    }

    private static I18nLiteral emptyLabel() {
        return new I18nLiteral() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return I18nLiteral.class;
            }

            @Override
            public String[] pairs() {
                return new String[0];
            }
        };
    }

    private static ConsoleCatalogResourceEntity dictionaryModule() {
        ConsoleCatalogResourceEntity module = new ConsoleCatalogResourceEntity();
        module.setResourceId("mod-1");
        module.setResourceType(CatalogResourceType.MODULE.name());
        module.setDomainKey("platform");
        module.setModuleKey("dictionary");
        module.setStatus(BasicStatus.ENABLED.name());
        return module;
    }

    private static PermissionGrantEntity grant(String grantKey) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey(grantKey);
        return grant;
    }

    private static PermissionGrantDao grantDaoForKey(String grantKey, boolean granted) {
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any()))
                .thenReturn(granted ? List.of(grant(grantKey)) : List.of());
        return grantDao;
    }
}
