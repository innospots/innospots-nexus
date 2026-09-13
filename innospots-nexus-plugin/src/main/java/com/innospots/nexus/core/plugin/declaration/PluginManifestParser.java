package com.innospots.nexus.core.plugin.declaration;

import java.io.InputStream;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 将 UTF-8 YAML 文档解析为严格的 {@link PluginManifest}。 */
public interface PluginManifestParser {

    /**
     * 解析一个 YAML 输入流。
     *
     * @param input UTF-8 YAML 文档输入流
     * @return 严格校验后的插件清单
     * @throws NexusException 语法或结构非法时抛出
     */
    PluginManifest parse(InputStream input);

    /**
     * 解析一个 UTF-8 YAML 文本。
     *
     * @param yaml UTF-8 YAML 文档文本
     * @return 严格校验后的插件清单
     * @throws NexusException 输入为空、语法或结构非法时抛出
     */
    default PluginManifest parse(String yaml) {
        if (yaml == null) {
            throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID, "yaml input is required");
        }
        return parse(new java.io.ByteArrayInputStream(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
