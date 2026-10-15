package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.logger.operator.AuditLogOperator;
import com.innospots.nexus.platform.logger.endpoint.PlatformAuditLogEndpoint;
import com.innospots.nexus.platform.logger.service.PlatformAuditLogService;

@Configuration
public class PlatformLoggerConfiguration {

    @Bean
    PlatformAuditLogService platformAuditLogService(AuditLogOperator auditLogOperator) {
        return new PlatformAuditLogService(auditLogOperator);
    }

    @Bean
    @Lazy
    PlatformAuditLogEndpoint platformAuditLogEndpoint(PlatformAuditLogService platformAuditLogService) {
        return new PlatformAuditLogEndpoint(platformAuditLogService);
    }
}
