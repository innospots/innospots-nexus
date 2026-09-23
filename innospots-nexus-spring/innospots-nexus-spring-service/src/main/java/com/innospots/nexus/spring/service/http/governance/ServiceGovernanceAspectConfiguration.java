package com.innospots.nexus.spring.service.http.governance;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.innospots.nexus.spring.service.http.invocation.GovernedInvocationExecutor;

/**
 * 启用基于注解的治理 AOP。
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "service.governance", name = "annotation-advice-enabled", matchIfMissing = true)
public class ServiceGovernanceAspectConfiguration {

    @Bean
    GovernedInvocationAspect governedInvocationAspect(GovernedInvocationExecutor governedInvocationExecutor) {
        return new GovernedInvocationAspect(governedInvocationExecutor);
    }
}
