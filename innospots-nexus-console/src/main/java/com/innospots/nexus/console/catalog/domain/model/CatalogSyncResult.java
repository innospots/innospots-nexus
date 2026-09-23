package com.innospots.nexus.console.catalog.domain.model;

/**
 * Console 目录显式同步结果。
 *
 * @author Smars
 * @date 2026/09/13
 */
public record CatalogSyncResult(
        int createdResources,
        int updatedResources,
        int disabledResources
) {
}
