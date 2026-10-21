package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

/**
 * MODULE 节点上的可授权面。
 */
public record CatalogModuleGrantVo(
        List<CatalogViewItemVo> viewItems,
        List<CatalogPermissionGroupVo> operationGroups
) {

    public CatalogModuleGrantVo {
        viewItems = viewItems == null ? List.of() : List.copyOf(viewItems);
        operationGroups = operationGroups == null ? List.of() : List.copyOf(operationGroups);
    }
}
