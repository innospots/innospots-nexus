package com.innospots.nexus.base.domain.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据操作的通用响应信封。通过 {@code success} 标志区分成功与失败，并携带可选 {@link DataSchema}、数据载荷与元数据。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DataRequest
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
     * 创建携带给定数据载荷的成功响应。
     */
    public static <T> DataResponse<T> ok(T data) {
        return new DataResponse<>(true, OK, OK, data);
    }

    /**
     * 创建带错误码与消息的失败响应。
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
     * 将给定映射合并到现有 meta 中。
     */
    public DataResponse<T> meta(Map<String, Object> meta) {
        if (meta != null) {
            this.meta.putAll(meta);
        }
        return this;
    }
}
