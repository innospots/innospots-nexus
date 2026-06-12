package com.innospots.nexus.base.ui.spec.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.innospots.nexus.base.exception.NexusException;

public final class UiSpecDslLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UiSpecDslLoader() {
    }

    public static UiSpecDsl fromYaml(String yaml) {
        try {
            return YAML_MAPPER.readValue(yaml, UiSpecDsl.class);
        } catch (JsonProcessingException e) {
            throw NexusException.build("UI_DSL_YAML_READ_FAILED", "Failed to read UI DSL YAML", e);
        }
    }

    public static String toYaml(UiSpecDsl dsl) {
        try {
            return YAML_MAPPER.writeValueAsString(dsl);
        } catch (JsonProcessingException e) {
            throw NexusException.build("UI_DSL_YAML_WRITE_FAILED", "Failed to write UI DSL YAML", e);
        }
    }
}
