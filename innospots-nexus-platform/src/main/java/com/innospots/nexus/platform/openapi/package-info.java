/**
 * Platform 模块 OpenAPI 构建期元数据与端点注解约定。
 *
 * <p>SmallRye 扫描 {@code com.innospots.nexus.platform} 生成 bundled YAML；
 * 运行时由 core {@code OpenApiCatalogEndpoint} 按 specId 提供。</p>
 *
 * @see com.innospots.nexus.platform.openapi.NexusPlatformOpenApiDefinition
 * JAX-RS 与操作注解位于各 {@code *.endpoint} 资源。
 */
package com.innospots.nexus.platform.openapi;
