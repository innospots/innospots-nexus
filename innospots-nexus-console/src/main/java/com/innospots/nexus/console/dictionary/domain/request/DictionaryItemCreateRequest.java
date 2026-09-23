package com.innospots.nexus.console.dictionary.domain.request;

/**
 * 在类型编码下创建字典项的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param itemValue 类型内唯一的稳定字典项值
 * @param itemName  显示名称
 * @param sortOrder 显示顺序
 */
public record DictionaryItemCreateRequest(
        String itemValue,
        String itemName,
        Integer sortOrder
) {
}
