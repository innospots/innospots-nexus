package com.innospots.nexus.spring.core.jaxrs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.scalar.OpenApiScalarDocumentation;
import com.scalar.maven.core.ScalarProperties;

/**
 * Scalar 文档 UI 与 OpenAPI 规范目录的 Spring 配置。
 *
 * <p>绑定 {@code scalar.*}（与 {@link ScalarProperties} 字段一致），并补充 Nexus HTTP 路由项。
 * 由 {@link NexusScalarJerseyConfiguration} 注册 Jersey 路由与 HTML 渲染。
 * 渲染时仅读取本 Bean；catalog 源等在 {@link OpenApiScalarDocumentation} 内写入配置快照，不会回写本单例。</p>
 *
 * <ul>
 *   <li>文档 HTML：{@link #getPath()}（默认 {@link OpenApiCatalogPaths#UI_DEFAULT}）</li>
 *   <li>前端脚本：{@link #resolveScalarJavascriptPath()}</li>
 *   <li>规范 JSON：{@link #getSpecsBase()} + {@code /{specId}}，与 {@link OpenApiCatalogEndpoint} 对齐</li>
 * </ul>
 *
 * @see NexusScalarJerseyConfiguration
 */
@ConfigurationProperties(prefix = "scalar")
public class OpenApiScalarSpringProperties extends ScalarProperties {

    /**
     * OpenAPI 规范目录根路径；须与 {@link OpenApiCatalogEndpoint} 的 {@code @Path} 一致。
     * 绑定 {@code scalar.specs-base}。
     */
    private String specsBase = OpenApiCatalogPaths.SPECS_BASE;

    /**
     * Scalar 前端脚本路径段（相对 {@link #getPath()}）；绑定 {@code scalar.script-segment}。
     */
    private String scriptSegment = OpenApiCatalogPaths.SCALAR_JS_SEGMENT;

    public OpenApiScalarSpringProperties() {
        OpenApiScalarDocumentation.applyNexusDefaults(this);
    }

    public String getSpecsBase() {
        return specsBase;
    }

    public void setSpecsBase(String specsBase) {
        this.specsBase = specsBase;
    }

    public String getScriptSegment() {
        return scriptSegment;
    }

    public void setScriptSegment(String scriptSegment) {
        this.scriptSegment = scriptSegment;
    }

    /**
     * 规范化后的 Scalar HTML 文档页路径。
     */
    public String resolveDocumentationPath() {
        return OpenApiCatalogPaths.normalizeDocumentationPath(getPath());
    }

    /**
     * Scalar {@code scalar.js} 的 HTTP 绝对路径。
     */
    public String resolveScalarJavascriptPath() {
        return OpenApiCatalogPaths.scalarJavascriptPath(resolveDocumentationPath(), scriptSegment);
    }

    /**
     * 单个 OpenAPI 规范 JSON 的 URL 前缀，形如 {@code /openapi/specs/}。
     */
    public String resolveSpecItemUrlPrefix() {
        return OpenApiCatalogPaths.specItemUrlPrefix(specsBase);
    }
}
