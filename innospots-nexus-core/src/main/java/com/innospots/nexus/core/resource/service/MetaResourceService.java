package com.innospots.nexus.core.resource.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.resources.FileResource;
import com.innospots.nexus.base.resources.MetaResource;
import com.innospots.nexus.base.resources.ResourceStore;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.resource.dao.MetaResourceDao;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;
import com.innospots.nexus.core.resource.storage.ResourceStorageRegistry;

/**
 * Coordinates binary storage backends with persisted file metadata.
 */
@RequiredArgsConstructor
public class MetaResourceService {

    private final MetaResourceDao metaResourceDao;
    private final ResourceStorageRegistry storageRegistry;

    /**
     * Stores a file and persists metadata when requested by the resource.
     *
     * @param resource  file payload
     * @param module    owning module name
     * @param moduleKey owning module key
     * @param storeMode optional storage backend mode
     * @return stored metadata
     */
    public MetaResource save(FileResource resource, String module, String moduleKey, String storeMode) {
        Checks.notNull(resource, "resource");
        Checks.notBlank(module, "module");
        Checks.notBlank(moduleKey, "moduleKey");
        ResourceStore store = storageRegistry.requireStore(storeMode);
        MetaResource stored = store.save(resource, module, moduleKey);
        if (!resource.saveMeta()) {
            return stored;
        }
        MetaResourceEntity entity = toEntity(stored);
        metaResourceDao.insert(entity);
        return stored;
    }

    /**
     * Reads binary content for a stored resource identifier.
     *
     * @param resourceId resource identifier
     * @return binary payload when found
     */
    public Optional<byte[]> read(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        if (entity == null) {
            return Optional.empty();
        }
        return storageRegistry.requireStore(entity.getStoreMode()).read(resourceId);
    }

    /**
     * Deletes a stored resource and its metadata row.
     *
     * @param resourceId resource identifier
     * @return whether a record existed and deletion succeeded
     */
    public boolean delete(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        if (entity == null) {
            return false;
        }
        boolean deleted = storageRegistry.requireStore(entity.getStoreMode()).delete(resourceId);
        if (deleted) {
            metaResourceDao.deleteById(resourceId);
        }
        return deleted;
    }

    /**
     * Finds persisted metadata by resource identifier.
     *
     * @param resourceId resource identifier
     * @return metadata when found
     */
    public Optional<MetaResource> findById(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        return entity == null ? Optional.empty() : Optional.of(toMetaResource(entity));
    }

    private static MetaResourceEntity toEntity(MetaResource resource) {
        MetaResourceEntity entity = new MetaResourceEntity();
        entity.setResourceId(resource.resourceId());
        entity.setResourceName(resource.resourceName());
        entity.setMimeType(resource.mimeType());
        entity.setFileSize(resource.fileSize());
        entity.setFileUri(resource.fileUri());
        entity.setUriKey(uriKey(resource.fileUri()));
        entity.setStoreMode(resource.storeMode());
        entity.setModule(resource.module());
        entity.setModuleKey(resource.moduleKey());
        return entity;
    }

    private static MetaResource toMetaResource(MetaResourceEntity entity) {
        return new MetaResource(
                entity.getResourceId(),
                entity.getResourceName(),
                entity.getMimeType(),
                entity.getModule(),
                entity.getModuleKey(),
                entity.getFileSize(),
                entity.getFileUri(),
                entity.getStoreMode(),
                entity.getCreatedAt() == null
                        ? Instant.EPOCH
                        : entity.getCreatedAt().toInstant(ZoneOffset.UTC));
    }

    private static String uriKey(String fileUri) {
        if (fileUri == null || fileUri.isBlank()) {
            return null;
        }
        int length = Math.min(fileUri.length(), 256);
        return fileUri.substring(0, length);
    }
}
