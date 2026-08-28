package com.innospots.nexus.core.plugin.resource;

/** Idempotent handle for one resource disposer. */
public interface ResourceRegistration extends AutoCloseable {

    /** Runs the disposer at most once. */
    @Override
    void close();
}
