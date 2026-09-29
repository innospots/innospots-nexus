package com.innospots.nexus.core.openapi.scalar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.core.openapi.catalog.OpenApiSpecItemVo;
import com.scalar.maven.core.ScalarHtmlRenderer;
import com.scalar.maven.core.ScalarProperties;
import com.scalar.maven.core.config.ScalarAgentOptions;
import com.scalar.maven.core.config.ScalarSource;
import com.scalar.maven.core.enums.ScalarLayout;
import com.scalar.maven.core.enums.ScalarTheme;

/**
 * Scalar 文档 UI 的框架无关配置与渲染，供 Spring、Quarkus 等运行时挂载 HTTP 路由时复用。
 */
public final class OpenApiScalarDocumentation {

    private OpenApiScalarDocumentation() {
    }

    public static ScalarProperties createDefaultProperties() {
        ScalarProperties properties = new ScalarProperties();
        applyNexusDefaults(properties);
        return properties;
    }

    /**
     * Nexus 平台推荐的 Scalar UI 默认值；Spring {@code scalar.*} 未配置时使用。
     */
    public static void applyNexusDefaults(ScalarProperties properties) {
        properties.setEnabled(true);
        properties.setPath(OpenApiCatalogPaths.UI_DEFAULT);
        properties.setPageTitle("Nexus API Reference");
        properties.setWithDefaultFonts(false);
        properties.setLayout(ScalarLayout.MODERN);
        properties.setTheme(ScalarTheme.DEFAULT);
        properties.setShowSidebar(true);
        properties.setDefaultOpenFirstTag(true);
        properties.setPersistAuth(true);
        properties.setTelemetry(false);
        properties.setHideTestRequestButton(false);
        properties.setDarkMode(false);
        properties.setHideDarkModeToggle(false);
        ScalarAgentOptions agent = new ScalarAgentOptions();
        agent.setDisabled(true);
        properties.setAgent(agent);
    }

    public static String normalizeDocsPath(String path) {
        return OpenApiCatalogPaths.normalizeDocumentationPath(path);
    }

    public static void applyCatalogSources(ScalarProperties scalarProperties, OpenApiCatalogOperator operator) {
        applyCatalogSources(scalarProperties, operator.listSpecs(), OpenApiCatalogPaths.SPECS_BASE);
    }

    public static void applyCatalogSources(ScalarProperties scalarProperties, List<OpenApiSpecItemVo> specs) {
        applyCatalogSources(scalarProperties, specs, OpenApiCatalogPaths.SPECS_BASE);
    }

    public static void applyCatalogSources(
            ScalarProperties scalarProperties,
            List<OpenApiSpecItemVo> specs,
            String specsBase) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        String specItemPrefix = OpenApiCatalogPaths.specItemUrlPrefix(specsBase);
        List<ScalarSource> sources = new ArrayList<>();
        boolean first = true;
        for (OpenApiSpecItemVo item : specs) {
            ScalarSource source = new ScalarSource();
            source.setUrl(specItemPrefix + item.specId());
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
        return renderDocumentationHtml(scalarProperties, operator, OpenApiCatalogPaths.SPECS_BASE);
    }

    public static String renderDocumentationHtml(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator,
            String specsBase) throws IOException {
        ScalarProperties prepared = prepareForServing(scalarProperties, operator, specsBase);
        return ScalarHtmlRenderer.render(prepared);
    }

    public static byte[] scalarJavascriptContent() throws IOException {
        return ScalarHtmlRenderer.getScalarJsContent();
    }

    public static ScalarProperties prepareForServing(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator) {
        return prepareForServing(scalarProperties, operator, OpenApiCatalogPaths.SPECS_BASE);
    }

    public static ScalarProperties prepareForServing(
            ScalarProperties scalarProperties,
            OpenApiCatalogOperator operator,
            String specsBase) {
        String docsPath = normalizeDocsPath(scalarProperties.getPath());
        scalarProperties.setPath(docsPath);
        applyCatalogSources(scalarProperties, operator.listSpecs(), specsBase);
        return scalarProperties;
    }
}
