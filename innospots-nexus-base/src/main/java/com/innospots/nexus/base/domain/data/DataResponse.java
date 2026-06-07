package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A generic response envelope for data operations. Distinguishes success
 * from failure via the {@code success} flag and carries an optional
 * {@link DataSchema}, data payload, and metadata.
 */
public class DataResponse<T> {

    public static final String OK = "OK";

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private DataSchema schema;
    private final Map<String, Object> meta = new LinkedHashMap<>();

    private DataResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a success response with the given data payload.
     */
    public static <T> DataResponse<T> ok(T data) {
        return new DataResponse<>(true, OK, OK, data);
    }

    /**
     * Creates a failure response with an error code and message.
     */
    public static <T> DataResponse<T> fail(String code, String message) {
        return new DataResponse<>(false, code, message, null);
    }

    public boolean success() {
        return success;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public T data() {
        return data;
    }

    public DataSchema schema() {
        return schema;
    }

    public DataResponse<T> schema(DataSchema schema) {
        this.schema = schema;
        return this;
    }

    public Map<String, Object> meta() {
        return Map.copyOf(meta);
    }

    public DataResponse<T> meta(String key, Object value) {
        meta.put(key, value);
        return this;
    }

    /**
     * Merges the given map into the existing meta.
     */
    public DataResponse<T> meta(Map<String, Object> meta) {
        if (meta != null) {
            this.meta.putAll(meta);
        }
        return this;
    }
}
