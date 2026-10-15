package com.innospots.nexus.console.logger.endpoint;

import java.lang.reflect.Method;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogEndpointContractsTest {

    @Test
    void auditLogEndpointIsContractWithoutClassPath() throws NoSuchMethodException {
        assertThat(AuditLogEndpoint.class.isInterface()).isTrue();
        assertThat(AuditLogEndpoint.class.getAnnotation(Path.class)).isNull();
        assertHttpMethod(AuditLogEndpoint.class, "pageAuditLogs", GET.class, AuditLogPageRequest.class);
        assertHttpMethod(AuditLogEndpoint.class, "getAuditLog", GET.class, String.class);
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
