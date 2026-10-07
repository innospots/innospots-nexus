package com.innospots.nexus.spring.console.role.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleJaxRsWebConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleRoleConfiguration;
import com.innospots.nexus.spring.core.bootstrap.NexusPersistenceConfiguration;
import com.innospots.nexus.spring.core.bootstrap.NexusTransactionConfiguration;

/**
 * 角色域 Spring 集成测试用最小应用（H2 文件库 + Jersey + 声明式事务）。
 */
@SpringBootApplication(scanBasePackageClasses = RoleEndpointIntegrationTestApplication.class)
@Import({
        NexusPersistenceConfiguration.class,
        NexusTransactionConfiguration.class,
        ConsoleRoleConfiguration.class,
        RoleEndpointJerseyIntegrationConfiguration.class,
        ConsoleJaxRsWebConfiguration.class,
        RoleEndpointIntegrationTestConfiguration.class
})
public class RoleEndpointIntegrationTestApplication {
}
