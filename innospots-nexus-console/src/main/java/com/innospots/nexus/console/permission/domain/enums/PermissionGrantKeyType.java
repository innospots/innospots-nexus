package com.innospots.nexus.console.permission.domain.enums;

import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 授权项键在 catalog 中的归属层级；持久化使用 {@link #code()}（{@code module} / {@code menu}）。
 *
 * @see CatalogResourceType
 */
public enum PermissionGrantKeyType {

    MODULE("module"),
    MENU("menu");

    private final String code;

    PermissionGrantKeyType(String code) {
        this.code = code;
    }

    /**
     * @return catalog 资源类型对应的存储码
     */
    public String code() {
        return code;
    }

    /**
     * 解析持久化列值；未知或空白时返回 {@code null}。
     */
    public static PermissionGrantKeyType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (PermissionGrantKeyType value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return value;
            }
        }
        return null;
    }
}
