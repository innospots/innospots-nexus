package com.innospots.nexus.console.bootstrap.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest;

/**
 * 初始用户清单的 YAML 解析器。
 *
 * @see InitialUserManifest
 */
public final class InitialUserManifestParser {

    private final ObjectMapper yamlMapper;

    /**
     * 创建解析器。
     */
    public InitialUserManifestParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        // 配置拼写错误（如 roelCodes）必须启动失败，否则初始用户会静默缺少角色。
    }

    /**
     * 解析初始用户清单。
     *
     * @param content      YAML 内容
     * @param resourcePath 资源路径，仅用于错误信息
     * @return 初始用户清单
     */
    public InitialUserManifest parse(String content, String resourcePath) {
        if (content == null || content.isBlank()) {
            return new InitialUserManifest(true, java.util.List.of());
        }
        try {
            InitialUserManifest manifest =
                    yamlMapper.readValue(content, InitialUserManifest.class);
            if (manifest == null) {
                return new InitialUserManifest(true, java.util.List.of());
            }
            return manifest.users() == null
                    ? new InitialUserManifest(manifest.enabled(), java.util.List.of())
                    : manifest;
        } catch (JsonProcessingException exception) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot parse initial user manifest: " + resourcePath,
                    exception);
        }
    }
}
