package com.innospots.nexus.spring.console.role.support;

import java.lang.annotation.Inherited;

import org.springframework.test.context.TestPropertySource;

/**
 * 角色集成测试公共属性：H2 文件库路径由 {@link RoleEndpointH2Support} 在运行时注入。
 */
@Inherited
@TestPropertySource(properties = {
        "logging.config=classpath:log4j2-test.xml"
})
public @interface RoleEndpointIntegrationTestProperties {
}
