package com.innospots.nexus.platform.logger.endpoint;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.logger.endpoint.AuditLogEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAuditLogEndpointContractsTest {

    @Test
    void implementsConsoleAuditLogEndpointOnPlatformPath() {
        assertThat(AuditLogEndpoint.class.isAssignableFrom(PlatformAuditLogEndpoint.class)).isTrue();
        assertThat(PlatformAuditLogEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.AUDIT_LOGS_PATH);
    }
}
