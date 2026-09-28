package com.innospots.nexus.console.dictionary.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 用于选择器的字典类型精简选项。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryTypeId type 标识符
 * @param typeCode         稳定的类型编码
 * @param typeName         显示名称
 */
@Schema(name = "DictionaryTypeOptionVo", description = "字典类型选项")
public record DictionaryTypeOptionVo(
        @Schema(description = "type 标识符", required = true)
        String dictionaryTypeId,
        @Schema(description = "稳定的类型编码", required = true)
        String typeCode,
        @Schema(description = "显示名称", required = true)
        String typeName
) {
}
