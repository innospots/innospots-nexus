package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对命名目标数据源执行 {@link DataOperation} 的请求。支持可选请求体、分页参数、自由格式查询映射及可扩展元数据映射。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DataOperation
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
     * 为给定目标与操作创建数据请求。
     *
     * @param target    数据源或实体标识
     * @param operation 要执行的操作类型
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
     * 用于对目标数据源进行身份认证的凭据键。
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
     * 设置分页参数。两个值必须为正数。
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
