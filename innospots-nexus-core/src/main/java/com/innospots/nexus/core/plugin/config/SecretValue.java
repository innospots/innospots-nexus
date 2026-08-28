package com.innospots.nexus.core.plugin.config;

import java.util.Arrays;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Closeable in-memory secret wrapper whose textual representation is always masked.
 */
public final class SecretValue implements AutoCloseable {

    private static final String MASK = "******";

    private final char[] value;

    private SecretValue(char[] value) {
        this.value = value;
    }

    /** Creates a defensive secret copy. */
    public static SecretValue of(String value) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "secret must not be blank");
        }
        return new SecretValue(value.toCharArray());
    }

    /**
     * Executes an operation against a temporary defensive copy of the characters.
     *
     * @param operation function that consumes the temporary character copy
     * @param <T> operation result type
     * @return operation result
     */
    public synchronized <T> T use(java.util.function.Function<char[], T> operation) {
        if (operation == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "secret operation is required");
        }
        char[] copy = Arrays.copyOf(value, value.length);
        try {
            return operation.apply(copy);
        } finally {
            Arrays.fill(copy, '\0');
        }
    }

    /** Returns an independently closeable copy without exposing the retained buffer. */
    public synchronized SecretValue copy() {
        return new SecretValue(Arrays.copyOf(value, value.length));
    }

    /** Clears the retained character buffer. */
    @Override
    public synchronized void close() {
        Arrays.fill(value, '\0');
    }

    @Override
    public String toString() {
        return MASK;
    }
}
