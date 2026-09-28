package com.innospots.nexus.console.dictionary.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新可变字典项字段的请求；项值不可变。
 *
 * @author Smars
 * @date 2026/09/13
 * @param itemName  显示名称
 * @param sortOrder 显示顺序
 */
@Schema(name = "DictionaryItemUpdateRequest", description = "更新字典项请求")
public record DictionaryItemUpdateRequest(
        @Schema(description = "显示名称", required = true)
        String itemName,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}
