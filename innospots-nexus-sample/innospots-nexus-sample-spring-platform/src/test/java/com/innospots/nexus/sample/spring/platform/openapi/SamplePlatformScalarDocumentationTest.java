package com.innospots.nexus.sample.spring.platform.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
import com.innospots.nexus.console.openapi.scalar.OpenApiScalarDocumentation;
import com.innospots.nexus.sample.spring.platform.openapi.support.OpenApiJerseyTestApplication;
import com.scalar.maven.core.ScalarProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 Scalar 文档页配置与 Spring Boot 嵌入式 HTTP 暴露。
 */
@SpringBootTest(classes = OpenApiJerseyTestApplication.class)
class SamplePlatformScalarDocumentationTest {

    private static final String PLATFORM_SPEC_ID = "innospots-nexus-platform";

    @Autowired
    private ScalarProperties scalarProperties;

    @Autowired
    private OpenApiCatalogOperator openApiCatalogOperator;

    @Test
    void scalarHtmlIncludesBundledPlatformSource() throws Exception {
        String html = OpenApiScalarDocumentation.renderDocumentationHtml(scalarProperties, openApiCatalogOperator);
        assertThat(html).contains("scalar");
        assertThat(html).contains(PLATFORM_SPEC_ID);
    }
}
