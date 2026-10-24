package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 访问或操作侧权限分组。
 */
public record CatalogPermissionGroupVo(
        String group,
        I18nObject displayName,
        Integer orderIndex,
        String resourceKey,
        List<CatalogViewItemVo> items
) {

    public CatalogPermissionGroupVo {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
