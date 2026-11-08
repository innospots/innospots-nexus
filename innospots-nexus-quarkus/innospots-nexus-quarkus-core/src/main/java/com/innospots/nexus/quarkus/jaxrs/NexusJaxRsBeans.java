package com.innospots.nexus.quarkus.jaxrs;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.innospots.nexus.core.jaxrs.exception.JaxRsExceptionSupport;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;

/**
 * Quarkus 宿主侧 JAX-RS 通用装配（异常映射与 OpenAPI 目录）。
 *
 * <p>{@link JaxRsExceptionSupport} 位于 framework-neutral 的 core 模块，没有 CDI 作用域注解；
 * 而 core 提供的 {@code NexusExceptionMapper} 等 {@code @Provider} 会被 Quarkus REST 自动发现并要求
 * 注入该依赖。这里显式提供一个应用级单例，避免 CDI 解析失败。</p>
 */
@ApplicationScoped
public class NexusJaxRsBeans {

    /**
     * 提供共享的 JAX-RS 异常响应构建支持。
     *
     * @return 异常支持实例
     */
    @Produces
    @ApplicationScoped
    JaxRsExceptionSupport jaxRsExceptionSupport() {
        return new JaxRsExceptionSupport();
    }

    /**
     * 提供 OpenAPI 规范目录读取器。
     *
     * <p>{@link OpenApiCatalogOperator} 同样位于 framework-neutral 的 core 模块；core 的
     * {@code OpenApiCatalogEndpoint} 被 Quarkus REST 自动发现后需要注入该依赖。</p>
     *
     * @return 目录读取器
     */
    @Produces
    @ApplicationScoped
    OpenApiCatalogOperator openApiCatalogOperator() {
        return new OpenApiCatalogOperator();
    }
}
