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
 * Page DSL 反序列化测试的共享 YAML 辅助工具。
 * @author Smars
 * @date 2026/09/13
 */
public final class PageDslYamlTestSupport {

    private PageDslYamlTestSupport() {
    }

    /**
     * 返回与 {@link JacksonPageDslParser} 相同配置的 YAML 映射器。
     *
     * @return 已配置的映射器
     */
    public static ObjectMapper yamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 返回反序列化后校验文档的严格解析器。
     *
     * @return 解析器
     */
    public static JacksonPageDslParser strictParser() {
        return new JacksonPageDslParser(PageDslConfig.defaults());
    }

    /**
     * 通过严格解析器解析完整页面文档。
     *
     * @param yaml 页面 YAML
     * @return 解析后的文档
     */
    public static PageDsl parsePage(String yaml) {
        return strictParser().parse(yaml);
    }

    /**
     * 将 YAML 反序列化为请求类型，不执行页面校验。
     *
     * @param yaml YAML 内容
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 反序列化结果
     */
    public static <T> T read(String yaml, Class<T> type) {
        try {
            return yamlMapper().readValue(yaml, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot read YAML into " + type.getSimpleName(), exception);
        }
    }

    /**
     * 使用共享映射器将值序列化为 YAML。
     *
     * @param value 待序列化的值
     * @return YAML 文本
     */
    public static String write(Object value) {
        try {
            return yamlMapper().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot write YAML 的" + value.getClass().getSimpleName(), exception);
        }
    }
}
