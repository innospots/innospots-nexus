package com.innospots.nexus.base.ui.spec.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.config.UiSpecConfig;
import com.innospots.nexus.base.ui.spec.validation.UiSpecValidator;

/** Jackson-based strict YAML parser for page specifications. */
public final class JacksonUiSpecParser implements UiSpecParser {

    private final ObjectMapper yamlMapper;
    private final UiSpecValidator validator;

    /** Creates a parser using the supplied loading and strictness configuration. */
    public JacksonUiSpecParser(UiSpecConfig config) {
        if (config == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec config is required");
        }
        this.yamlMapper = configure(new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)), config);
        this.validator = new UiSpecValidator();
    }

    /** Parses and validates YAML content. */
    @Override
    public UiSpec parse(String content) {
        if (content == null || content.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec YAML content is required");
        }
        try {
            UiSpec spec = yamlMapper.readValue(content, UiSpec.class);
            validator.validate(spec);
            return spec;
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot parse UiSpec YAML",
                    exception);
        }
    }

    /** Serializes a validated specification as YAML. */
    @Override
    public String write(UiSpec spec) {
        validator.validate(spec);
        try {
            return yamlMapper.writeValueAsString(spec);
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.SERIALIZATION_FAILED.fullCode(),
                    "Cannot serialize UiSpec YAML",
                    exception);
        }
    }

    private ObjectMapper configure(ObjectMapper mapper, UiSpecConfig config) {
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                config.failOnUnknownProperties());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
