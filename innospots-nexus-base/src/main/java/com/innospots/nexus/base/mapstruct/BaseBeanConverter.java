package com.innospots.nexus.base.mapstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.util.DateTimeUtils;

/**
 * 领域模型与持久化实体之间的 MapStruct 风格转换器基接口。
 * 提供列表转换、JSON 字符串与 Map/List 互转以及日期时间格式化的默认方法。
 *
 * @author Smars
 * @date 2026/09/13
 * @param <Model>  领域模型类型
 * @param <Entity> 持久化实体类型
 * @see BaseMapperSupport
 * @see BaseMapperConfig
 */
public interface BaseBeanConverter<Model, Entity> {

    /**
     * 将实体转换为领域模型。
     *
     * @param entity 持久化实体
     * @return 领域模型
     */
    Model entityToModel(Entity entity);

    /**
     * 将领域模型转换为实体。
     *
     * @param model 领域模型
     * @return 持久化实体
     */
    Entity modelToEntity(Model model);

    /**
     * 批量将实体列表转换为领域模型列表。
     *
     * @param entities 实体列表
     * @return 领域模型列表
     */
    default List<Model> entitiesToModels(List<Entity> entities) {
        return BaseMapperSupport.mapList(entities, this::entityToModel);
    }

    /**
     * 批量将领域模型列表转换为实体列表。
     *
     * @param models 领域模型列表
     * @return 实体列表
     */
    default List<Entity> modelsToEntities(List<Model> models) {
        return BaseMapperSupport.mapList(models, this::modelToEntity);
    }

    /**
     * 将 JSON 字符串解析为 Map。
     *
     * @param json JSON 字符串
     * @return 键值映射
     */
    default Map<String, Object> jsonStrToMap(String json) {
        return Jsons.toMap(json);
    }

    /**
     * 将 Map 序列化为 JSON 字符串。
     *
     * @param map 键值映射
     * @return JSON 字符串
     */
    default String mapToJsonStr(Map<String, Object> map) {
        return Jsons.toJson(map);
    }

    /**
     * 将 JSON 字符串解析为字符串 Map。
     *
     * @param json JSON 字符串
     * @return 字符串键值映射
     */
    default Map<String, String> jsonStrToMapStr(String json) {
        return Jsons.fromJson(json, new TypeReference<>() {
        });
    }

    /**
     * 将字符串 Map 序列化为 JSON 字符串。
     *
     * @param map 字符串键值映射
     * @return JSON 字符串
     */
    default String mapStrToJsonStr(Map<String, String> map) {
        return Jsons.toJson(map);
    }

    /**
     * 将 JSON 字符串解析为字符串列表。
     *
     * @param json JSON 字符串
     * @return 字符串列表
     */
    default List<String> jsonStrToList(String json) {
        return Jsons.fromJsonList(json, String.class);
    }

    /**
     * 将字符串列表序列化为 JSON 字符串。
     *
     * @param list 字符串列表
     * @return JSON 字符串
     */
    default String listToJsonStr(List<String> list) {
        return Jsons.toJson(list);
    }

    /**
     * 将 JSON 字符串解析为 Map 列表。
     *
     * @param json JSON 字符串
     * @return Map 列表
     */
    default List<Map<String, Object>> jsonStrToListMap(String json) {
        return Jsons.fromJson(json, new TypeReference<>() {
        });
    }

    /**
     * 将 Map 列表序列化为 JSON 字符串。
     *
     * @param list Map 列表
     * @return JSON 字符串
     */
    default String listMapToJsonStr(List<Map<String, Object>> list) {
        return Jsons.toJson(list);
    }

    /**
     * 将 JSON 字符串解析为整数列表。
     *
     * @param json JSON 字符串
     * @return 整数列表
     */
    default List<Integer> jsonToIntList(String json) {
        return Jsons.fromJsonList(json, Integer.class);
    }

    /**
     * 将整数列表序列化为 JSON 字符串。
     *
     * @param list 整数列表
     * @return JSON 字符串
     */
    default String jsonIntToString(List<Integer> list) {
        return Jsons.toJson(list);
    }

    /**
     * 将 {@link LocalDateTime} 格式化为毫秒精度字符串。
     *
     * @param localDateTime 日期时间
     * @return 格式化字符串
     */
    default String localDateTimeToStr(LocalDateTime localDateTime) {
        return DateTimeUtils.format(localDateTime, DateTimeUtils.DATETIME_MS_PATTERN);
    }

    /**
     * 将毫秒精度字符串解析为 {@link LocalDateTime}。
     *
     * @param localDateTime 日期时间字符串
     * @return 解析后的日期时间
     */
    default LocalDateTime strToLocalDateTime(String localDateTime) {
        return DateTimeUtils.parseLocalDateTime(localDateTime, DateTimeUtils.DATETIME_MS_PATTERN);
    }
}
