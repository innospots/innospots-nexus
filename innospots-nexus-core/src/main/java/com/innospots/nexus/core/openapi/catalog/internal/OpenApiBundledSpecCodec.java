package com.innospots.nexus.core.openapi.catalog.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 将构建期打包的 OpenAPI YAML 正文解析为 JSON 树（HTTP 响应用 {@code application/json}）。
 */
public final class OpenApiBundledSpecCodec {

    private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();

    private OpenApiBundledSpecCodec() {
    }

    public static JsonNode parseYamlDocument(String yaml) {
        try {
            return YAML_MAPPER.readTree(yaml);
        } catch (JsonProcessingException exception) {
            throw NexusException.build(
                    NexusStatusCode.SYSTEM_ERROR.fullCode(),
                    "Failed to parse OpenAPI YAML document",
                    exception);
        }
    }
}
