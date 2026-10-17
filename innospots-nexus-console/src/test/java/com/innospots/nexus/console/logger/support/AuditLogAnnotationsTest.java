package com.innospots.nexus.console.logger.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.logger.AuditLog;

class AuditLogAnnotationsTest {

    @Test
    void resolvesAuditLogDeclaredOnInterfaceMethod() throws NoSuchMethodException {
        Method method = DictionaryTypeEndpoint.class.getMethod(
                "createDictionaryType",
                DictionaryTypeCreateRequest.class);

        AuditLog auditLog = AuditLogAnnotations.resolve(method, null);

        assertThat(auditLog).isNotNull();
        assertThat(auditLog.action()).isEqualTo("dictionary.type.create");
    }
}
