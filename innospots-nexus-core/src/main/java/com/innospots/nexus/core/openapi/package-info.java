/**
 * OpenAPI 共享约定与运行时目录。
 *
 * <ul>
 *   <li>构建期：各 API 模块将规范打包为 {@code META-INF/nexus-openapi/<artifactId>.yaml}</li>
 *   <li>运行时：{@link com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint} 暴露规范目录 HTTP</li>
 *   <li>文档页：{@link com.innospots.nexus.core.openapi.scalar.OpenApiScalarDocumentation}（Scalar）</li>
 * </ul>
 */
package com.innospots.nexus.core.openapi;
