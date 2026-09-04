package com.innospots.nexus.core.plugin.contribution.console.catalog.domain.model;

/** Console 目录显式同步结果。 */
public record CatalogSyncResult(
        int createdResources,
        int updatedResources,
        int disabledResources
) {
}
