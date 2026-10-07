package com.innospots.nexus.core.openapi.catalog;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.innospots.nexus.core.openapi.catalog.internal.OpenApiBundledSpecCodec;
import com.innospots.nexus.core.openapi.catalog.internal.OpenApiBundledSpecs;

/**
 * 按文件名加载 {@link OpenApiBundledSpecs#RESOURCE_ROOT} 下的 OpenAPI YAML。
 */
public final class OpenApiCatalogOperator {

    private final ClassLoader classLoader;

    public OpenApiCatalogOperator() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public OpenApiCatalogOperator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<OpenApiSpecItemVo> listSpecs() {
        return OpenApiBundledSpecs.listSpecIds(classLoader).stream()
                .map(specId -> new OpenApiSpecItemVo(specId, specId + ".yaml"))
                .toList();
    }

    public String readYaml(String specId) {
        return OpenApiBundledSpecs.readYaml(classLoader, specId);
    }

    /**
     * 读取打包 YAML 并解析为 OpenAPI 文档 JSON（供 HTTP {@code application/json} 响应）。
     */
    public JsonNode readOpenApiDocument(String specId) {
        return OpenApiBundledSpecCodec.parseYamlDocument(readYaml(specId));
    }
}
