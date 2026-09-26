package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;
import org.mockito.Mockito;

/**
 * 供页面权限 Filter 测试替换的 {@link ConsolePagePermissionAuthorizer} 模拟。
 */
public final class ConsoleJaxRsWebTestMocks {

    public static final ConsolePagePermissionAuthorizer PAGE_PERMISSION_AUTHORIZER =
            Mockito.mock(ConsolePagePermissionAuthorizer.class);

    private ConsoleJaxRsWebTestMocks() {
    }
}
