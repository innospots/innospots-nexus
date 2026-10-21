package com.innospots.nexus.console.catalog.domain.vo;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 可勾选的访问或操作授权项。
 */
public record CatalogViewItemVo(
        String resourceKey,
        String grantName,
        I18nObject displayName,
        I18nObject description,
        Integer orderIndex
) {
}
