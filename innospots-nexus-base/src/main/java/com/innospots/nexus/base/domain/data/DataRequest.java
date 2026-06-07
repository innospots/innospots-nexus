package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A request to perform a {@link DataOperation} on a named target datasource.
 * Supports an optional request body, pagination parameters, a free-form
 * query map, and a metadata map for extensibility.
 */
public class DataRequest<T> {

    private final String target;
    private final DataOperation operation;
    private String credentialKey;
    private T body;
    private int pageNo = 1;
    private int pageSize = 20;
    private final Map<String, Object> query = new LinkedHashMap<>();
    private final Map<String, Object> meta = new LinkedHashMap<>();

    private DataRequest(String target, DataOperation operation) {
        this.target = target;
        this.operation = operation;
    }

    /**
     * Creates a data request for the given target and operation.
     *
     * @param target    datasource or entity identifier
     * @param operation the type of operation to perform
     */
    public static <T> DataRequest<T> create(String target, DataOperation operation) {
        return new DataRequest<>(target, operation);
    }

    public String target() {
        return target;
    }

    public DataOperation operation() {
        return operation;
    }

    /**
     * The credential key used for authentication against the target datasource.
     */
    public String credentialKey() {
        return credentialKey;
    }

    public DataRequest<T> credentialKey(String credentialKey) {
        this.credentialKey = credentialKey;
        return this;
    }

    public T body() {
        return body;
    }

    public DataRequest<T> body(T body) {
        this.body = body;
        return this;
    }

    public int pageNo() {
        return pageNo;
    }

    public int pageSize() {
        return pageSize;
    }

    /**
     * Sets pagination parameters. Both values must be positive.
     */
    public DataRequest<T> page(int pageNo, int pageSize) {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        return this;
    }

    public Map<String, Object> query() {
        return Map.copyOf(query);
    }

    public Object query(String key) {
        return query.get(key);
    }

    public DataRequest<T> query(String key, Object value) {
        query.put(key, value);
        return this;
    }

    public Map<String, Object> meta() {
        return Map.copyOf(meta);
    }

    public DataRequest<T> meta(String key, Object value) {
        meta.put(key, value);
        return this;
    }
}
