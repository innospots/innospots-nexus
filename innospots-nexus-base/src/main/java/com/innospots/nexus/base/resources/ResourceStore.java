package com.innospots.nexus.base.resources;

import java.io.IOException;
import java.util.Optional;

/**
 * Abstraction for persisting and retrieving binary resources.
 * Implementations may store locally (file system), remotely (S3, OSS),
 * or in a database. Each store has a unique {@link #storeMode()} identifier.
 */
public interface ResourceStore {

    MetaResource save(FileResource resource, String module, String moduleKey);

    Optional<byte[]> read(String resourceId);

    boolean delete(String resourceId);

    boolean exists(String resourceId);

    String storeMode();
}
