package com.innospots.nexus.base.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-Local Context — a typed {@link ThreadLocal} map for propagating
 * cross-cutting state (trace ID, tenant ID, user ID, project ID, etc.)
 * across asynchronous boundaries.
 * <p>Use {@link #scope(Map)} with try-with-resources for scoped context
 * injection:</p>
 * <pre>{@code
 * try (var scope = TLC.scope(Map.of(TLC.TRACE_ID, "abc"))) {
 *     // code running with the injected context
 * }
 * }</pre>
 */
public final class TLC {

    public static final String TRACE_ID = "traceId";
    public static final String TENANT_ID = "tenantId";
    public static final String SESSION_ID = "sessionId";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String PROJECT_ID = "projectId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(LinkedHashMap::new);

    private TLC() {
    }

    /** Sets the project ID in context. Passing null removes the entry. */
    public static void projectId(Long projectId) {
        put(PROJECT_ID, projectId);
    }

    /** Returns the project ID from context, or null. */
    public static Long projectId() {
        return getLong(PROJECT_ID);
    }

    /** Sets the user ID in context. Passing null removes the entry. */
    public static void userId(Long userId) {
        put(USER_ID, userId);
    }

    /** Returns the user ID from context, or null. */
    public static Long userId() {
        return getLong(USER_ID);
    }

    /** Sets the user name in context. Passing null removes the entry. */
    public static void userName(String userName) {
        put(USER_NAME, userName);
    }

    /** Returns the user name from context, or null. */
    public static String userName() {
        return getString(USER_NAME);
    }

    /**
     * Sets a context value. A null value removes the key from context
     * (equivalent to calling {@link #remove}).
     */
    public static void put(String key, Object value) {
        if (value == null) {
            remove(key);
        } else {
            CONTEXT.get().put(key, value);
        }
    }

    /**
     * Puts all entries from the given map into context.
     * Null values in the map will remove the corresponding keys.
     */
    public static void putAll(Map<String, ?> values) {
        if (values == null) {
            return;
        }
        values.forEach(TLC::put);
    }

    /** Gets a context value by key. */
    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    /** Gets a context value as a String, or null. */
    public static String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Gets a context value as a Long, with type-safe conversion:
     * Long passthrough, Number -> longValue(), String -> parseLong().
     */
    public static Long getLong(String key) {
        Object value = get(key);
        return switch (value) {
            case null -> null;
            case Long longValue -> longValue;
            case Number number -> number.longValue();
            case String text when !text.isBlank() -> Long.parseLong(text);
            default -> null;
        };
    }

    /** Removes a key from context. */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /** Captures a snapshot of the current context (defensive copy). */
    public static Map<String, Object> snapshot() {
        return new LinkedHashMap<>(CONTEXT.get());
    }

    /** Replaces the entire context with the given map. */
    public static void restore(Map<String, ?> context) {
        Map<String, Object> next = new LinkedHashMap<>();
        if (context != null) {
            context.forEach(next::put);
        }
        CONTEXT.set(next);
    }

    /**
     * Creates a scoped context: merges the given values into the current
     * context, returning a {@link Scope} that restores the previous state
     * when closed. Use with try-with-resources:
     * <pre>{@code
     * try (var s = TLC.scope(Map.of("txId", "abc"))) { ... }
     * }</pre>
     */
    public static Scope scope(Map<String, ?> values) {
        Map<String, Object> previous = snapshot();
        Map<String, Object> next = new HashMap<>(previous);
        if (values != null) {
            next.putAll(values);
        }
        restore(next);
        return new Scope(previous);
    }

    /** Returns the raw context map (shared reference, not a copy). */
    public static Map<String, Object> context() {
        return CONTEXT.get();
    }

    /** Removes all context values for the current thread. */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * AutoCloseable scope that restores the previous context on close.
     * Used internally by {@link TLC#scope(Map)}.
     */
    public record Scope(Map<String, Object> previous) implements AutoCloseable {

        @Override
        public void close() {
            TLC.restore(previous);
        }
    }
}
