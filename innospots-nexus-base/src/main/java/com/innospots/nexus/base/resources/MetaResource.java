package com.innospots.nexus.base.resources;

import java.time.Instant;

/**
 * Immutable metadata record for a stored resource. Links a resource to
 * a module context ({@code module} + {@code moduleKey}) and records
 * storage details (URI, store mode, creation time).
 */
public record MetaResource(
        String resourceId,
        String resourceName,
        String mimeType,
        String module,
        String moduleKey,
        long fileSize,
        String fileUri,
        String storeMode,
        Instant createdAt
) {
}
