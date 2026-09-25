package com.innospots.nexus.console.openapi.scalar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.openapi.domain.vo.OpenApiSpecItemVo;
import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
import com.scalar.maven.core.ScalarHtmlRenderer;
import com.scalar.maven.core.ScalarProperties;
import com.scalar.maven.core.config.ScalarAgentOptions;
import com.scalar.maven.core.config.ScalarSource;

/**
 * Scalar 文档 UI 的框架无关配置与渲染，供 Spring、Quarkus 等运行时挂载 HTTP 路由时复用。
 */
public final class OpenApiScalarDocumentation {

    public static final String DEFAULT_DOCS_PATH = "/openapi/ui";

    public static final String SPEC_URL_PREFIX = "/openapi/specs/";

    private OpenApiScalarDocumentation() {
    }

    public static ScalarProperties createDefaultProperties() {
        ScalarProperties properties = new ScalarProperties();
        properties.setEnabled(true);
        properties.setPath(DEFAULT_DOCS_PATH);
        properties.setPageTitle("Nexus API Reference");
        properties.setWithDefaultFonts(false);
        ScalarAgentOptions agent = new ScalarAgentOptions();
        agent.setDisabled(true);
        properties.setAgent(agent);
        return properties;
    }

    public static String normalizeDocsPath(String path) {
        if (path == null || path.isBlank()) {
            return DEFAULT_DOCS_PATH;
        }
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public static void applyCatalogSources(ScalarProperties scalarProperties, OpenApiCatalogOperator operator) {
        applyCatalogSources(scalarProperties, operator.listSpecs());
    }

    public static void applyCatalogSources(ScalarProperties scalarProperties, List<OpenApiSpecItemVo> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        List<ScalarSource> sources = new ArrayList<>();
        boolean first = true;
        for (OpenApiSpecItemVo item : specs) {
            ScalarSource source = new ScalarSource();
            source.setUrl(SPEC_URL_PREFIX + item.specId());
            source.setTitle(item.specId());
            source.setSlug(item.specId());
            source.setDefault(first);
            ScalarAgentOptions agent = new ScalarAgentOptions();
            agent.setDisabled(true);
            source.setAgent(agent);
            sources.add(source);
            first = false;
        }
        scalarProperties.setSources(sources);
    }

    /**
     * 规范化文档路径、绑定 catalog 源后渲染 HTML。
     */
    public static String renderDocumentationHtml(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator) throws IOException {
        ScalarProperties prepared = prepareForServing(scalarProperties, operator);
        return ScalarHtmlRenderer.render(prepared);
    }

    public static byte[] scalarJavascriptContent() throws IOException {
        return ScalarHtmlRenderer.getScalarJsContent();
    }

    public static ScalarProperties prepareForServing(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator) {
        String docsPath = normalizeDocsPath(scalarProperties.getPath());
        scalarProperties.setPath(docsPath);
        applyCatalogSources(scalarProperties, operator);
        return scalarProperties;
    }
}
