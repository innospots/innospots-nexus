package com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator;

/**
 * Jackson-based strict YAML parser for Pactor page DSL documents.
 *
 * <p>Parsing honors {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig#failOnUnknownProperties()}
 * and always runs {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator} before
 * returning a document.</p>
 */
public final class JacksonPageDslParser implements PageDslParser {

    private final ObjectMapper yamlMapper;
    private final PageDslValidator validator;

    /**
     * Creates a parser using the supplied loading and strictness configuration.
     *
     * @param config parser configuration
     */
    public JacksonPageDslParser(PageDslConfig config) {
        if (config == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl config is required");
        }
        this.yamlMapper = configure(new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)), config);
        this.validator = new PageDslValidator();
    }

    @Override
    public PageDsl parse(String content) {
        if (content == null || content.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl YAML content is required");
        }
        try {
            PageDsl document = yamlMapper.readValue(content, PageDsl.class);
            validator.validate(document);
            return document;
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot parse PageDsl YAML",
                    exception);
        }
    }

    @Override
    public String write(PageDsl document) {
        validator.validate(document);
        try {
            return yamlMapper.writeValueAsString(document);
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.SERIALIZATION_FAILED.fullCode(),
                    "Cannot serialize PageDsl YAML",
                    exception);
        }
    }

    private ObjectMapper configure(ObjectMapper mapper, PageDslConfig config) {
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                config.failOnUnknownProperties());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
