package com.innospots.nexus.platform.support.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.support.domain.request.SupportAccessGrantCreateRequest;
import com.innospots.nexus.platform.support.domain.vo.SupportAccessGrantVo;

import static org.assertj.core.api.Assertions.assertThat;

class SupportAccessGrantEndpointContractsTest {

    @Test
    void supportAccessEndpointExposesOpsGrantLifecycle() throws NoSuchMethodException {
        assertThat(SupportAccessGrantEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/platform/support-access");
        assertHttpMethod(SupportAccessGrantEndpoint.class, "createGrant",
                POST.class, SupportAccessGrantCreateRequest.class);
        assertHttpMethod(SupportAccessGrantEndpoint.class, "getGrant", GET.class, String.class);
    }

    @Test
    void supportAccessRequestsAndViewsAreImmutableRecords() {
        assertThat(SupportAccessGrantCreateRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(SupportAccessGrantCreateRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly("tenantId", "platformUserId", "reason", "expireAt");
        assertThat(SupportAccessGrantVo.class.isRecord()).isTrue();
        assertThat(Arrays.stream(SupportAccessGrantVo.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "grantId",
                        "tenantId",
                        "platformUserId",
                        "reason",
                        "approvedBy",
                        "expireAt",
                        "status");
        assertThat(SupportAccessGrantCreateRequest.class.getRecordComponents()[3].getType())
                .isEqualTo(LocalDateTime.class);
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
