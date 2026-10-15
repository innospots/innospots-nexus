package com.innospots.nexus.console.dictionary.domain.request;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 更新可变字典类型字段的请求；类型编码不可变。
 *
 * @author Smars
 * @date 2026/09/13
 * @param typeName  显示名称
 * @param sortOrder 显示顺序
 * @param items     非 null 时全量替换该类型下字典项
 */
@Schema(name = "DictionaryTypeUpdateRequest", description = "更新字典类型请求")
public record DictionaryTypeUpdateRequest(
        @Schema(description = "多语言显示名称（须含 zh-CN）")
        I18nObject typeName,
        @Schema(description = "显示顺序")
        Integer sortOrder,
        @Schema(description = "全量替换的字典项列表；省略则不改动字典项")
        List<DictionaryItemCreateRequest> items
) {
    public DictionaryTypeUpdateRequest {
        if (items != null) {
            items = List.copyOf(items);
        }
    }
}
