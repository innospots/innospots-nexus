/**
 * 统一 API 响应形状（{@link R}、{@link PageResult}）。
 *
 * <p>端点方法返回 {@code R<T>} 或 {@code R<PageResult<T>>} 时，SmallRye 在扫描 JAX-RS 时会根据
 * 方法签名中的类型实参展开 {@code data} 字段；本模块在构建期生成 Jandex 索引并标注
 * {@link org.eclipse.microprofile.openapi.annotations.media.Schema}，供依赖方 OpenAPI 生成读取。</p>
 */
package com.innospots.nexus.base.domain.response;
