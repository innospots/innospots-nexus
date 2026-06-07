package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A self-timing data payload. Automatically records the start time at
 * construction and computes elapsed millis when {@link #end()} is called.
 * Carries optional {@link DataSchema} metadata and a free-form meta map.
 */
public class DataBody<T> {

    private final long startTimeMillis = System.currentTimeMillis();
    private T data;
    private DataSchema schema;
    private String message;
    private long elapsedMillis;
    private final Map<String, Object> meta = new LinkedHashMap<>();

    private DataBody(T data) {
        this.data = data;
    }

    /**
     * Wraps data into a new DataBody with automatic start-time recording.
     */
    public static <T> DataBody<T> of(T data) {
        return new DataBody<>(data);
    }

    public T data() {
        return data;
    }

    public DataBody<T> data(T data) {
        this.data = data;
        return this;
    }

    public DataSchema schema() {
        return schema;
    }

    public DataBody<T> schema(DataSchema schema) {
        this.schema = schema;
        return this;
    }

    public String message() {
        return message;
    }

    public DataBody<T> message(String message) {
        this.message = message;
        return this;
    }

    public long elapsedMillis() {
        return elapsedMillis;
    }

    /**
     * Stops the timer and records elapsed milliseconds since construction.
     */
    public DataBody<T> end() {
        this.elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        return this;
    }

    public Map<String, Object> meta() {
        return Map.copyOf(meta);
    }

    public DataBody<T> meta(String key, Object value) {
        meta.put(key, value);
        return this;
    }
}
