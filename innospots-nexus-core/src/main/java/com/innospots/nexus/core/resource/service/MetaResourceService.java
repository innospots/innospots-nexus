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
import com.innospots.nexus.core.persistence.scope.OwnershipScope;
import com.innospots.nexus.core.persistence.scope.PersistenceOwnership;
import com.innospots.nexus.core.resource.dao.MetaResourceDao;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;
import com.innospots.nexus.core.resource.storage.ResourceStorageRegistry;

/**
 * 协调二进制存储后端与持久化文件元数据。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetaResourceDao
 * @see ResourceStorageRegistry
 */
@RequiredArgsConstructor
public class MetaResourceService {

    private final MetaResourceDao metaResourceDao;
    private final ResourceStorageRegistry storageRegistry;

    /**
     * 存储文件，并在资源要求时持久化元数据。
     *
     * @param resource  文件载荷
     * @param module    所属模块名称
     * @param moduleKey 所属模块键
     * @param storeMode 可选存储后端模式
     * @return 存储后的元数据
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
        OwnershipScope.stamp(entity, OwnershipScope.captureFromSession());
        metaResourceDao.insert(entity);
        return stored;
    }

    /**
     * 按资源标识读取二进制内容。
     *
     * @param resourceId 资源标识
     * @return 找到时的二进制载荷
     */
    public Optional<byte[]> read(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        if (entity == null) {
            return Optional.empty();
        }
        assertReadable(entity);
        return storageRegistry.requireStore(entity.getStoreMode()).read(resourceId);
    }

    /**
     * 删除已存储资源及其元数据行。
     *
     * @param resourceId 资源标识
     * @return 记录存在且删除成功时返回 {@code true}
     */
    public boolean delete(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        if (entity == null) {
            return false;
        }
        assertReadable(entity);
        boolean deleted = storageRegistry.requireStore(entity.getStoreMode()).delete(resourceId);
        if (deleted) {
            metaResourceDao.deleteById(resourceId);
        }
        return deleted;
    }

    /**
     * 按资源标识查找持久化元数据。
     *
     * @param resourceId 资源标识
     * @return 找到时的元数据
     */
    public Optional<MetaResource> findById(String resourceId) {
        Checks.notBlank(resourceId, "resourceId");
        MetaResourceEntity entity = metaResourceDao.selectById(resourceId);
        if (entity == null) {
            return Optional.empty();
        }
        assertReadable(entity);
        return Optional.of(toMetaResource(entity));
    }

    private static void assertReadable(MetaResourceEntity entity) {
        PersistenceOwnership ownership = OwnershipScope.captureFromSession();
        OwnershipScope.assertOwnership(entity, ownership);
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
