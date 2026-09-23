package com.innospots.nexus.console.dictionary.domain.request;

/**
 * 更新可变字典类型字段的请求；类型编码不可变。
 *
 * @author Smars
 * @date 2026/09/13
 * @param typeName  显示名称
 * @param sortOrder 显示顺序
 */
public record DictionaryTypeUpdateRequest(
        String typeName,
        Integer sortOrder
) {
}
