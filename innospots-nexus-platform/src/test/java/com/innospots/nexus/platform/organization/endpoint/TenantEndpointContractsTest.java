package com.innospots.nexus.platform.organization.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.organization.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantPageRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantStatusUpdateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantUpdateRequest;
import com.innospots.nexus.platform.organization.domain.vo.TenantVo;

import static org.assertj.core.api.Assertions.assertThat;

class TenantEndpointContractsTest {

    @Test
    void tenantEndpointExposesPlatformOrganizationLifecycle() throws NoSuchMethodException {
        assertThat(TenantEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(com.innospots.nexus.platform.config.PlatformConstant.TENANTS_PATH);
        assertThat(TenantEndpoint.class.getAnnotation(Tag.class).name()).isEqualTo("PlatformOrganization");
        assertThat(TenantEndpoint.class.getAnnotation(NexusAuthenticatedApi.class)).isNotNull();
        assertThat(TenantEndpoint.class.isInterface()).isFalse();
        assertThat(TenantEndpoint.class.getMethod("pageTenants", TenantPageRequest.class)
                .getAnnotation(Operation.class).operationId()).isEqualTo("platformTenantPage");
        assertThat(TenantEndpoint.class.getMethod("createTenant", TenantCreateRequest.class)
                .getAnnotation(Operation.class).operationId()).isEqualTo("platformTenantCreate");
        assertHttpMethod(TenantEndpoint.class, "pageTenants", GET.class, TenantPageRequest.class);
        assertHttpMethod(TenantEndpoint.class, "createTenant", POST.class, TenantCreateRequest.class);
        assertHttpMethod(TenantEndpoint.class, "getTenant", GET.class, String.class);
        assertHttpMethod(
                TenantEndpoint.class,
                "updateTenant",
                PUT.class,
                String.class,
                TenantUpdateRequest.class);
        assertHttpMethod(
                TenantEndpoint.class,
                "updateTenantStatus",
                PUT.class,
                String.class,
                TenantStatusUpdateRequest.class);
        assertThat(Arrays.stream(TenantEndpoint.class.getMethods()).map(Method::getName))
                .contains("pageTenants", "createTenant", "getTenant", "updateTenant", "updateTenantStatus");
    }

    @Test
    void tenantRequestsAndViewsAreImmutableRecords() {
        assertThat(TenantCreateRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(TenantCreateRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "tenantName",
                        "tenantCode",
                        "tenantType",
                        "planCode",
                        "ownerTenantUserId");
        assertThat(TenantVo.class.isRecord()).isTrue();
        assertThat(Arrays.stream(TenantVo.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "tenantId",
                        "tenantName",
                        "tenantCode",
                        "tenantType",
                        "status",
                        "planCode",
                        "ownerTenantUserId",
                        "enterpriseId",
                        "legalName");
    }

    private static void assertHttpMethod(
            Class<?> endpointType,
            String methodName,
            Class<? extends java.lang.annotation.Annotation> httpAnnotation,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = endpointType.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(httpAnnotation)).isNotNull();
    }
}
