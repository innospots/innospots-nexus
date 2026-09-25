package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * JAX-RS Web 过滤器/异常映射 HTTP 集成测试基类。
 */
@ConsoleJaxRsWebIntegrationTestProperties
@SpringBootTest(
        classes = ConsoleJaxRsWebTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ConsoleJaxRsWebIntegrationTest {

    @LocalServerPort
    protected int port;

    protected ConsoleJaxRsWebHttpClient http;

    @BeforeEach
    void initHttpClient() {
        http = new ConsoleJaxRsWebHttpClient(port);
    }
}
