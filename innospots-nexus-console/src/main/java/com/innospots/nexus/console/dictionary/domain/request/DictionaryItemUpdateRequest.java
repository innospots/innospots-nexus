package com.innospots.nexus.console.dictionary.domain.request;

/**
 * 更新可变字典项字段的请求；项值不可变。
 *
 * @author Smars
 * @date 2026/09/13
 * @param itemName  显示名称
 * @param sortOrder 显示顺序
 */
public record DictionaryItemUpdateRequest(
        String itemName,
        Integer sortOrder
) {
}
