package com.innospots.nexus.core.resource.service;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.resources.FileResource;
import com.innospots.nexus.base.resources.MetaResource;
import com.innospots.nexus.base.resources.ResourceStore;
import com.innospots.nexus.core.resource.dao.MetaResourceDao;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;
import com.innospots.nexus.core.resource.storage.ResourceStorageRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetaResourceServiceTest {

    @Test
    void savePersistsMetadataWhenSaveMetaIsEnabled() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        MetaResourceDao dao = mock(MetaResourceDao.class);
        when(dao.insert(any(MetaResourceEntity.class))).thenReturn(1);
        MetaResourceService service = new MetaResourceService(
                dao, new ResourceStorageRegistry().register(store));

        MetaResource saved = service.save(
                new FileResource("demo", "demo.txt", "text/plain", new ByteArrayInputStream(new byte[] {1}), true),
                "docs",
                "readme",
                null);

        assertThat(saved.storeMode()).isEqualTo("memory");
        verify(dao).insert(any(MetaResourceEntity.class));
    }

    @Test
    void readUsesRegisteredStoreModeFromMetadata() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        store.save(
                new FileResource("demo", "demo.txt", "text/plain", new ByteArrayInputStream(new byte[] {9}), false),
                "docs",
                "readme");
        MetaResourceDao dao = mock(MetaResourceDao.class);
        MetaResourceEntity entity = new MetaResourceEntity();
        entity.setResourceId("res-1");
        entity.setStoreMode("memory");
        when(dao.selectById("res-1")).thenReturn(entity);
        MetaResourceService service = new MetaResourceService(
                dao, new ResourceStorageRegistry().register(store));

        assertThat(service.read("res-1")).contains(new byte[] {9});
    }

    private static final class InMemoryResourceStore implements ResourceStore {

        private final Map<String, byte[]> payloads = new HashMap<>();

        @Override
        public MetaResource save(FileResource resource, String module, String moduleKey) {
            String resourceId = "res-1";
            payloads.put(resourceId, new byte[] {9});
            return new MetaResource(
                    resourceId,
                    resource.name(),
                    resource.contentType(),
                    module,
                    moduleKey,
                    1,
                    "memory://" + resourceId,
                    storeMode(),
                    Instant.EPOCH);
        }

        @Override
        public Optional<byte[]> read(String resourceId) {
            return Optional.ofNullable(payloads.get(resourceId));
        }

        @Override
        public boolean delete(String resourceId) {
            return payloads.remove(resourceId) != null;
        }

        @Override
        public boolean exists(String resourceId) {
            return payloads.containsKey(resourceId);
        }

        @Override
        public String storeMode() {
            return "memory";
        }
    }
}
