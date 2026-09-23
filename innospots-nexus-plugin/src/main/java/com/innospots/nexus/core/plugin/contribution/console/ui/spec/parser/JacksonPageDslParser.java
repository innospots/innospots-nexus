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
 * 基于 Jackson 的 Pactor 页面 DSL 严格 YAML 解析器。
 *
 * <p>解析遵循
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig#failOnUnknownProperties()}，
 * 并在返回文档前始终运行
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.validation.PageDslValidator}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class JacksonPageDslParser implements PageDslParser {

    private final ObjectMapper yamlMapper;
    private final PageDslValidator validator;

    /**
     * 使用提供的加载与严格性配置创建解析器。
     *
     * @param config 解析器配置
     * @throws com.innospots.nexus.base.exception.NexusException 配置为 null 时
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
