package com.innospots.nexus.console.sitemap.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;

/**
 * 基于 Jackson 的 sitemap YAML 解析器。
 */
public final class SitemapYamlParser {

    private final ObjectMapper yamlMapper;

    public SitemapYamlParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 将 YAML 文本解析为配置文档。
     *
     * @param content YAML 内容
     * @return 配置根对象
     */
    public SitemapConfig parse(String content) {
        if (content == null || content.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Sitemap YAML content is required");
        }
        try {
            return yamlMapper.readValue(content, SitemapConfig.class);
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot parse sitemap YAML",
                    exception);
        }
    }
}
