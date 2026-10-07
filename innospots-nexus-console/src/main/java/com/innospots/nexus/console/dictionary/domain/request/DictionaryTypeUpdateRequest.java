package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新可变字典类型字段的请求；类型编码不可变。
 *
 * @author Smars
 * @date 2026/09/13
 * @param typeName  显示名称
 * @param sortOrder 显示顺序
 */
@Schema(name = "DictionaryTypeUpdateRequest", description = "更新字典类型请求")
public record DictionaryTypeUpdateRequest(
        @Schema(description = "显示名称", required = true)
        String typeName,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}
