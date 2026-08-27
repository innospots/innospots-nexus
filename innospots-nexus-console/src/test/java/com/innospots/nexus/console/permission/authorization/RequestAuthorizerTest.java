package com.innospots.nexus.console.permission.authorization;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.dao.PermissionResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.entity.PermissionResourceEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionResourceType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestAuthorizerTest {

    @Test
    void checksPageThenDatasourceAndMergesRoleAndOrgUnitConstraints() {
        PermissionResourceEntity page = resource(
                "page-1", PermissionResourceType.PAGE, "page:sales.orders");
        page.setPageKey("sales.orders");
        PermissionResourceEntity datasource = resource(
                "datasource-1", PermissionResourceType.DATASOURCE,
                "datasource:sales.orders.approve");
        datasource.setPageKey("sales.orders");
        datasource.setDatasourceKey("approve");
        datasource.setRequestMethod("POST");
        datasource.setRequestUrl("/api/orders/{orderId}/approve");
        PermissionGrantEntity pageGrant = grant("page-1", null);
        PermissionGrantEntity roleGrant = grant("datasource-1", "region = EAST");
        PermissionGrantEntity orgUnitGrant = grant("datasource-1", "tenant = NORTH");

        PermissionResourceDao resourceDao = mock(PermissionResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(page), List.of(datasource));
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(
                List.of(pageGrant),
                List.of(),
                List.of(roleGrant),
                List.of(orgUnitGrant));

        RequestAuthorizer authorizer = new RequestAuthorizer(resourceDao, grantDao);
        AuthorizationDecision decision = authorizer.authorize(new AuthorizationRequest(
                "1",
                "post",
                "/api/orders/ORD-1001/approve?trace=true",
                "sales.orders",
                new AuthorizationSubject(
                        "user-1", Set.of("role-1"), Set.of("org-1"), false)));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.context().datasourceKey()).isEqualTo("approve");
        assertThat(decision.context().constraintDefinitions())
                .containsExactly("region = EAST", "tenant = NORTH");
    }

    @Test
    void deniesBeforeDatasourceLookupWhenPageGrantIsMissing() {
        PermissionResourceEntity page = resource(
                "page-1", PermissionResourceType.PAGE, "page:sales.orders");
        PermissionResourceDao resourceDao = mock(PermissionResourceDao.class);
        when(resourceDao.selectList(any())).thenReturn(List.of(page));
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of());

        AuthorizationDecision decision = new RequestAuthorizer(
                resourceDao, grantDao).authorize(new AuthorizationRequest(
                "1",
                "GET",
                "/api/orders",
                "sales.orders",
                new AuthorizationSubject("user-1", Set.of("role-1"), Set.of(), false)));

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.denyReason()).isEqualTo("Page permission is required");
    }

    private static PermissionResourceEntity resource(
            String resourceId,
            PermissionResourceType type,
            String resourceKey
    ) {
        PermissionResourceEntity resource = new PermissionResourceEntity();
        resource.setResourceId(resourceId);
        resource.setResourceType(type.name());
        resource.setResourceKey(resourceKey);
        resource.setStatus("ENABLED");
        return resource;
    }

    private static PermissionGrantEntity grant(String resourceId, String constraint) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setResourceId(resourceId);
        grant.setConstraintDefinition(constraint);
        return grant;
    }
}
