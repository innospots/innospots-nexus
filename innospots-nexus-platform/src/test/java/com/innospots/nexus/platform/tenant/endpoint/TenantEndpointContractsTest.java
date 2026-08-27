package com.innospots.nexus.platform.tenant.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.tenant.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.tenant.domain.vo.TenantVo;

import static org.assertj.core.api.Assertions.assertThat;

class TenantEndpointContractsTest {

    @Test
    void tenantEndpointExposesPlatformTenantLifecycle() throws NoSuchMethodException {
        assertThat(TenantEndpoint.class.getAnnotation(Path.class).value()).isEqualTo("/platform/tenants");
        assertHttpMethod(TenantEndpoint.class, "createTenant", POST.class, TenantCreateRequest.class);
        assertHttpMethod(TenantEndpoint.class, "getTenant", GET.class, String.class);
    }

    @Test
    void tenantRequestsAndViewsAreImmutableRecords() {
        assertThat(TenantCreateRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(TenantCreateRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "tenantName",
                        "tenantCode",
                        "planCode",
                        "ownerTenantUserId",
                        "legalName",
                        "creditCode",
                        "industry",
                        "contactName",
                        "contactPhone",
                        "contactEmail",
                        "address");
        assertThat(TenantVo.class.isRecord()).isTrue();
        assertThat(Arrays.stream(TenantVo.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "tenantId",
                        "tenantName",
                        "tenantCode",
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
