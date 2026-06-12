package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.innospots.nexus.base.exception.NexusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central JSON utility facade built on Jackson. Provides two {@link ObjectMapper}
 * instances: a default mapper and a {@link #maskedMapper()} with the
 * {@link MaskingModule} registered for field-level value conversion and masking.
 */
public final class Jsons {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    /** Mapper with {@link MaskingModule} registered for field-level conversion and masking support. */
    private static final ObjectMapper MASKED_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .addModule(new MaskingModule())
            .build();

    private Jsons() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Returns an {@link ObjectMapper} with {@link MaskingModule} registered,
     * so that fields annotated with {@link ValueConverter} or {@link MaskValue}
     * are automatically transformed during serialization.
     *
     * @return a mapper that respects {@code @ValueConverter} and {@code @MaskValue} annotations
     * @see ValueConverter
     * @see MaskStrategy
     * @see MaskValue
     */
    public static ObjectMapper maskedMapper() {
        return MASKED_MAPPER;
    }

    /**
     * Serialize an object to JSON, applying field conversion and masking where
     * {@code @ValueConverter} or {@code @MaskValue} annotations are present.
     *
     * @see #maskedMapper()
     */
    public static String toMaskedJson(Object value) {
        return toJson(MASKED_MAPPER, value);
    }

    public static String toJson(Object value) {
        return toJson(MAPPER, value);
    }

    private static String toJson(ObjectMapper mapper, Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw NexusException.build("JSON_WRITE_FAILED", "Failed to write JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw NexusException.build("JSON_READ_FAILED", "Failed to read JSON", e);
        }
    }

    /**
     * Deserializes a JSON string to a typed List.
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elementType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw NexusException.build("JSON_READ_FAILED", "Failed to read JSON list", e);
        }
    }

    /**
     * Deserializes a JSON string to a typed Set.
     */
    public static <T> Set<T> fromJsonSet(String json, Class<T> elementType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory()
                    .constructCollectionType(Set.class, elementType));
        } catch (JsonProcessingException e) {
            throw NexusException.build("JSON_READ_FAILED", "Failed to read JSON set", e);
        }
    }

    /**
     * Deserializes a JSON string to a {@code Map<String, Object>}.
     */
    public static Map<String, Object> toMap(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw NexusException.build("JSON_READ_FAILED", "Failed to read JSON map", e);
        }
    }
}
