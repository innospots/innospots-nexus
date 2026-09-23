package com.innospots.nexus.base.domain.field;

/**
 * 具有存储值与显示标签的可选项。
 *
 * @author Smars
 * @date 2026/09/13
 * @param value 存储值
 * @param label 显示标签
 * @see DomainField
 */
public record SelectOption(
        String value,
        String label
) {
}
