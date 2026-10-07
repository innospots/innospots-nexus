package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于 Jackson 的中央 JSON 工具门面。提供两个 {@link ObjectMapper} 实例：
 * 默认映射器，以及注册了 {@link MaskingModule} 的 {@link #maskedMapper()}，
 * 支持字段级值转换与脱敏。
 *
 * @author Smars
 * @date 2026/09/13
 * @see I18nModule
 * @see MaskingModule
 * @see MaskValue
 * @see ValueConverter
 */
public final class Jsons {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndAddModules()
            .addModule(new I18nModule())
            .build();

    /** 注册了 {@link MaskingModule} 的映射器，支持字段级转换与脱敏。 */
    private static final ObjectMapper MASKED_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndAddModules()
            .addModule(new I18nModule())
            .addModule(new MaskingModule())
            .build();

    private Jsons() {
    }

    /**
     * 返回默认 {@link ObjectMapper}。
     *
     * @return 默认映射器
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 返回注册了 {@link MaskingModule} 的 {@link ObjectMapper}，
     * 标注了 {@link ValueConverter} 或 {@link MaskValue} 的字段在序列化时自动转换。
     *
     * @return 支持 {@code @ValueConverter} 和 {@code @MaskValue} 注解的映射器
     * @see ValueConverter
     * @see MaskStrategy
     * @see MaskValue
     */
    public static ObjectMapper maskedMapper() {
        return MASKED_MAPPER;
    }

    /**
     * 将对象序列化为 JSON，对标注了 {@code @ValueConverter} 或 {@code @MaskValue} 的字段应用转换与脱敏。
     *
     * @param value 待序列化的对象
     * @return JSON 字符串
     * @see #maskedMapper()
     */
    public static String toMaskedJson(Object value) {
        return toJson(MASKED_MAPPER, value);
    }

    /**
     * 将对象序列化为 JSON。
     *
     * @param value 待序列化的对象
     * @return JSON 字符串
     */
    public static String toJson(Object value) {
        return toJson(MAPPER, value);
    }

    private static String toJson(ObjectMapper mapper, Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象。
     *
     * @param json JSON 文本
     * @param type 目标类型
     * @param <T>  目标类型参数
     * @return 反序列化后的对象
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为类型化 List。
     *
     * @param json        JSON 文本
     * @param elementType 元素类型
     * @param <T>         元素类型参数
     * @return 反序列化后的列表
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elementType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为类型化 Set。
     *
     * @param json        JSON 文本
     * @param elementType 元素类型
     * @param <T>         元素类型参数
     * @return 反序列化后的集合
     */
    public static <T> Set<T> fromJsonSet(String json, Class<T> elementType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory()
                    .constructCollectionType(Set.class, elementType));
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为 {@code Map<String, Object>}。
     *
     * @param json JSON 文本
     * @return 键值映射
     */
    public static Map<String, Object> toMap(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为由 {@link TypeReference} 描述的类型化值。
     *
     * @param json JSON 文本
     * @param type 目标类型引用
     * @param <T>  目标类型
     * @return 反序列化后的值
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, e);
        }
    }
}
