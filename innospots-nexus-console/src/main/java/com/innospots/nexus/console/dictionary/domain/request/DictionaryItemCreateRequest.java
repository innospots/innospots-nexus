package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 在类型编码下创建字典项的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param itemValue 类型内唯一的稳定字典项值
 * @param itemName  显示名称
 * @param sortOrder 显示顺序
 */
@Schema(name = "DictionaryItemCreateRequest", description = "创建字典项请求")
public record DictionaryItemCreateRequest(
        @Schema(description = "类型内唯一的稳定字典项值", required = true)
        String itemValue,
        @Schema(description = "显示名称", required = true)
        String itemName,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}
