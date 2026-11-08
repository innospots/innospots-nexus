package com.innospots.nexus.console.bootstrap.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 角色授权清单的 YAML 解析器。
 *
 * @see RoleAuthorizationManifest
 */
public final class RoleAuthorizationManifestParser {

    private final ObjectMapper yamlMapper;

    /**
     * 创建解析器。
     */
    public RoleAuthorizationManifestParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        this.yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        // 配置拼写错误（如 roleNames）必须启动失败，否则会静默丢失意图。
    }

    /**
     * 解析角色授权清单。
     *
     * @param content     YAML 内容
     * @param resourcePath 资源路径，仅用于错误信息
     * @return 角色授权清单
     */
    public RoleAuthorizationManifest parse(String content, String resourcePath) {
        if (content == null || content.isBlank()) {
            return new RoleAuthorizationManifest(java.util.List.of());
        }
        try {
            RoleAuthorizationManifest manifest =
                    yamlMapper.readValue(content, RoleAuthorizationManifest.class);
            return manifest == null || manifest.roles() == null
                    ? new RoleAuthorizationManifest(java.util.List.of())
                    : manifest;
        } catch (JsonProcessingException exception) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot parse role authorization manifest: " + resourcePath,
                    exception);
        }
    }
}
