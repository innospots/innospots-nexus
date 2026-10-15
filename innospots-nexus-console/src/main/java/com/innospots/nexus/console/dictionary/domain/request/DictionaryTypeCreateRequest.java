package com.innospots.nexus.console.dictionary.domain.request;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 创建字典类型的请求（不含归属与安全域；由调用域 service 固化）。
 *
 * @param typeCode  归属范围内唯一的稳定类型编码
 * @param typeName  显示名称
 * @param sortOrder 显示顺序
 * @param items     可选初始字典项，与类型在同一事务内写入
 */
@Schema(name = "DictionaryTypeCreateRequest", description = "创建字典类型请求")
public record DictionaryTypeCreateRequest(
        @Schema(description = "归属范围内唯一的稳定类型编码", required = true)
        String typeCode,
        @Schema(description = "多语言显示名称（须含 zh-CN）", required = true)
        I18nObject typeName,
        @Schema(description = "显示顺序")
        Integer sortOrder,
        @Schema(description = "初始字典项列表")
        List<DictionaryItemCreateRequest> items
) {
    public DictionaryTypeCreateRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
