/**
 * OpenAPI {@link org.eclipse.microprofile.openapi.annotations.media.Schema} 约定。
 *
 * <p>Jakarta REST 请求/响应 DTO 使用 {@code record}（或少数 {@code @BeanParam} Bean），
 * 在类型与各组件上标注 {@code @Schema}，由 SmallRye 在 {@code process-classes} 生成 components。</p>
 *
 * <p>端点返回 {@code R<T>} / {@code R<PageResult<T>>} 时，无需为每个接口手写 response schema：
 * JAX-RS 扫描器读取方法 {@link java.lang.reflect.Type}，将类型实参绑定到 {@code R.data}、
 * {@code PageResult.records}。{@code innospots-nexus-base} 中的 {@code R}、{@code PageResult}、
 * {@code I18nObject} 等共享类型须带 {@code @Schema}，且 base JAR 须包含 Jandex 索引
 * （{@code jandex-maven-plugin}），以便 {@code scanDependenciesDisable=false} 的模块解析依赖中的泛型 record。</p>
 */
package com.innospots.nexus.core.openapi.schema;
