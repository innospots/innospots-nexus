package com.innospots.nexus.base.execution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Execution context for a single run. Contains immutable input parameters
 * ({@code inputs}) and a mutable working memory ({@code context}) that
 * executors can read and write during execution.
 */
public class ExecutionContext {

    private final String executionId;
    private final Map<String, Object> inputs;
    private final Map<String, Object> context;

    protected ExecutionContext(String executionId) {
        this(executionId, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    protected ExecutionContext(String executionId, Map<String, Object> inputs, Map<String, Object> context) {
        this.executionId = executionId;
        this.inputs = inputs == null ? new LinkedHashMap<>() : new LinkedHashMap<>(inputs);
        this.context = context == null ? new LinkedHashMap<>() : new LinkedHashMap<>(context);
    }

    /**
     * Creates a context with the given execution ID and empty inputs/context maps.
     */
    public static ExecutionContext create(String executionId) {
        return new ExecutionContext(executionId);
    }

    public String executionId() {
        return executionId;
    }

    /**
     * Sets an input parameter. Inputs are part of the immutable execution boundary.
     */
    public ExecutionContext input(String key, Object value) {
        inputs.put(key, value);
        return this;
    }

    /** Gets an input parameter by key. */
    public Object getInput(String key) {
        return inputs.get(key);
    }

    /** Gets an input parameter as a String, or null. */
    public String getInputString(String key) {
        Object value = getInput(key);
        return value == null ? null : String.valueOf(value);
    }

    /** Gets an input parameter as an Integer, or null. */
    public Integer getInputInteger(String key) {
        return toInteger(getInput(key));
    }

    /** Gets an input parameter as a Long, or null. */
    public Long getInputLong(String key) {
        return toLong(getInput(key));
    }

    /** Returns an unmodifiable view of all input parameters. */
    public Map<String, Object> inputs() {
        return Map.copyOf(inputs);
    }

    /**
     * Puts a value into the mutable working context. Unlike inputs, context
     * can be read and written by executors during execution.
     */
    public ExecutionContext put(String key, Object value) {
        context.put(key, value);
        return this;
    }

    /** Gets a context value by key. */
    public Object get(String key) {
        return context.get(key);
    }

    /** Gets a context value as a String, or null. */
    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** Gets a context value as an Integer, or null. */
    public Integer getInteger(String key) {
        return toInteger(get(key));
    }

    /** Gets a context value as a Long, or null. */
    public Long getLong(String key) {
        return toLong(get(key));
    }

    /** Returns an unmodifiable view of the mutable context map. */
    public Map<String, Object> context() {
        return Map.copyOf(context);
    }

    /**
     * Converts a raw value to Integer using type-safe pattern matching:
     * Integer passthrough, Number -> intValue(), String -> parseInt(), others -> null.
     */
    private static Integer toInteger(Object value) {
        return switch (value) {
            case null -> null;
            case Integer integer -> integer;
            case Number number -> number.intValue();
            case String text when !text.isBlank() -> Integer.parseInt(text);
            default -> null;
        };
    }

    /**
     * Converts a raw value to Long: Long passthrough, Number -> longValue(),
     * String -> parseLong(), others -> null.
     */
    private static Long toLong(Object value) {
        return switch (value) {
            case null -> null;
            case Long longValue -> longValue;
            case Number number -> number.longValue();
            case String text when !text.isBlank() -> Long.parseLong(text);
            default -> null;
        };
    }
}
