package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import com.innospots.nexus.console.permission.authorization.RequestAuthorizer;

import org.mockito.Mockito;

/**
 * 供 datasource 鉴权测试替换的 {@link RequestAuthorizer} 模拟。
 */
public final class ConsoleJaxRsWebTestMocks {

    public static final RequestAuthorizer REQUEST_AUTHORIZER = Mockito.mock(RequestAuthorizer.class);

    private ConsoleJaxRsWebTestMocks() {
    }
}
