package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;
import org.mockito.Mockito;

/**
 * 供资源权限 Filter 测试替换的 {@link ConsoleResourcePermissionAuthorizer} 模拟。
 */
public final class ConsoleJaxRsWebTestMocks {

    public static final ConsoleResourcePermissionAuthorizer RESOURCE_PERMISSION_AUTHORIZER =
            Mockito.mock(ConsoleResourcePermissionAuthorizer.class);

    private ConsoleJaxRsWebTestMocks() {
    }
}
