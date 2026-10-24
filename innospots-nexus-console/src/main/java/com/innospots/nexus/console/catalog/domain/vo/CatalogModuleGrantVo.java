package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

/**
 * MODULE 节点上的可授权面。
 *
 * <p>{@code viewGroups} 来自 MODULE {@code viewGroups} 声明（组级授权）；
 * {@code operationGroups} 来自 {@code EndpointModuleGrantCatalog#operable()}。</p>
 */
public record CatalogModuleGrantVo(
        List<CatalogPermissionGroupVo> viewGroups,
        List<CatalogPermissionGroupVo> operationGroups
) {

    public CatalogModuleGrantVo {
        viewGroups = viewGroups == null ? List.of() : List.copyOf(viewGroups);
        operationGroups = operationGroups == null ? List.of() : List.copyOf(operationGroups);
    }
}
