package com.innospots.nexus.base.mapstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Base MapStruct-like converter interface between domain models and
 * persistence entities. Provides default methods for converting lists,
 * JSON strings to maps/lists, and date/time formatting.
 *
 * @param <Model>  the domain model type
 * @param <Entity> the persistence entity type
 */
public interface BaseBeanConverter<Model, Entity> {

    Model entityToModel(Entity entity);

    Entity modelToEntity(Model model);

    default List<Model> entitiesToModels(List<Entity> entities) {
        return BaseMapperSupport.mapList(entities, this::entityToModel);
    }

    default List<Entity> modelsToEntities(List<Model> models) {
        return BaseMapperSupport.mapList(models, this::modelToEntity);
    }

    default Map<String, Object> jsonStrToMap(String json) {
        return Jsons.toMap(json);
    }

    default String mapToJsonStr(Map<String, Object> map) {
        return Jsons.toJson(map);
    }

    default Map<String, String> jsonStrToMapStr(String json) {
        try {
            return Jsons.mapper().readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new NexusException("JSON_READ_FAILED", "Failed to read JSON string map", e);
        }
    }

    default String mapStrToJsonStr(Map<String, String> map) {
        return Jsons.toJson(map);
    }

    default List<String> jsonStrToList(String json) {
        return Jsons.fromJsonList(json, String.class);
    }

    default String listToJsonStr(List<String> list) {
        return Jsons.toJson(list);
    }

    default List<Map<String, Object>> jsonStrToListMap(String json) {
        try {
            return Jsons.mapper().readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new NexusException("JSON_READ_FAILED", "Failed to read JSON map list", e);
        }
    }

    default String listMapToJsonStr(List<Map<String, Object>> list) {
        return Jsons.toJson(list);
    }

    default List<Integer> jsonToIntList(String json) {
        return Jsons.fromJsonList(json, Integer.class);
    }

    default String jsonIntToString(List<Integer> list) {
        return Jsons.toJson(list);
    }

    default String localDateTimeToStr(LocalDateTime localDateTime) {
        return DateTimeUtils.format(localDateTime, DateTimeUtils.DATETIME_MS_PATTERN);
    }

    default LocalDateTime strToLocalDateTime(String localDateTime) {
        return DateTimeUtils.parseLocalDateTime(localDateTime, DateTimeUtils.DATETIME_MS_PATTERN);
    }
}
