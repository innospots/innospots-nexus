package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 创建字典类型的请求（不含归属与安全域；由调用域 service 固化）。
 *
 * @param typeCode  归属范围内唯一的稳定类型编码
 * @param typeName  显示名称
 * @param sortOrder 显示顺序
 */
@Schema(name = "DictionaryTypeCreateRequest", description = "创建字典类型请求")
public record DictionaryTypeCreateRequest(
        @Schema(description = "归属范围内唯一的稳定类型编码", required = true)
        String typeCode,
        @Schema(description = "显示名称", required = true)
        String typeName,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}
