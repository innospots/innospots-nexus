package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自带计时的数据载荷。构造时自动记录开始时间，调用 {@link #end()} 时计算耗时毫秒数。携带可选 {@link DataSchema} 元数据与自由格式 meta 映射。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DataSchema
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
     * 将数据包装为自动记录开始时间的新 DataBody。
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
     * 停止计时并记录自构造以来的耗时毫秒数。
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
