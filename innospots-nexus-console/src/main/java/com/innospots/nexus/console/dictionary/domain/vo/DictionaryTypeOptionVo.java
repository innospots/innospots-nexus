package com.innospots.nexus.console.dictionary.domain.vo;

/**
 * 用于选择器的字典类型精简选项。
 *
 * @author Smars
 * @date 2026/09/13
 * @param dictionaryTypeId type 标识符
 * @param typeCode         稳定的类型编码
 * @param typeName         显示名称
 */
public record DictionaryTypeOptionVo(
        String dictionaryTypeId,
        String typeCode,
        String typeName
) {
}
