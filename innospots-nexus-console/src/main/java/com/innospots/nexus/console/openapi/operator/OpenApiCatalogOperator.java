package com.innospots.nexus.console.openapi.operator;

import java.util.List;

import com.innospots.nexus.console.openapi.domain.vo.OpenApiSpecItemVo;
import com.innospots.nexus.console.openapi.internal.OpenApiBundledSpecs;

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
}
