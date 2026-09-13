package com.innospots.nexus.core.plugin.contribution.console.ui.spec.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.JacksonPageDslParser;

/**
 * Shared YAML helpers for Page DSL deserialization tests.
 */
public final class PageDslYamlTestSupport {

    private PageDslYamlTestSupport() {
    }

    /**
     * Returns a YAML mapper configured like {@link JacksonPageDslParser}.
     *
     * @return configured mapper
     */
    public static ObjectMapper yamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Returns a strict parser that validates documents after deserialization.
     *
     * @return parser
     */
    public static JacksonPageDslParser strictParser() {
        return new JacksonPageDslParser(PageDslConfig.defaults());
    }

    /**
     * Parses a full page document through the strict parser.
     *
     * @param yaml page YAML
     * @return parsed document
     */
    public static PageDsl parsePage(String yaml) {
        return strictParser().parse(yaml);
    }

    /**
     * Deserializes YAML into the requested type without running page validation.
     *
     * @param yaml YAML content
     * @param type target type
     * @param <T> target type
     * @return deserialized value
     */
    public static <T> T read(String yaml, Class<T> type) {
        try {
            return yamlMapper().readValue(yaml, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot read YAML into " + type.getSimpleName(), exception);
        }
    }

    /**
     * Serializes a value to YAML using the shared mapper.
     *
     * @param value value to serialize
     * @return YAML text
     */
    public static String write(Object value) {
        try {
            return yamlMapper().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot write YAML for " + value.getClass().getSimpleName(), exception);
        }
    }
}
