package com.innospots.nexus.core.plugin.declaration;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 基于 Jackson YAML 的严格 DSL 解析器，限制输入大小、深度和 YAML 扩展语法。 */
public final class JacksonPluginManifestParser implements PluginManifestParser {

    /** DSL 单文档最大字节数。 */
    public static final int MAX_DOCUMENT_BYTES = 1024 * 1024;

    /** DSL 最大嵌套深度。 */
    public static final int MAX_NESTING_DEPTH = 64;

    private static final Pattern YAML_EXTENSION = Pattern.compile(
            "(?m)(?:^|\\s)(?:[&*][A-Za-z_][A-Za-z0-9_-]*|![A-Za-z_][A-Za-z0-9_:/.-]*)");
    private final ObjectMapper mapper;

    /** 使用固定约束创建解析器。 */
    public JacksonPluginManifestParser() {
        YAMLFactory factory = YAMLFactory.builder()
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .streamReadConstraints(StreamReadConstraints.builder()
                        .maxNestingDepth(MAX_NESTING_DEPTH)
                        .maxDocumentLength(MAX_DOCUMENT_BYTES)
                        .build())
                .build();
        mapper = new ObjectMapper(factory)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    /** 返回用于测试或扩展绑定的严格 ObjectMapper。 */
    public ObjectMapper mapper() {
        return mapper.copy();
    }

    /**
     * 解析一个 UTF-8 YAML 输入流。
     *
     * @param input UTF-8 YAML 文档输入流
     * @return 严格校验后的插件清单
     * @throws NexusException 输入为空、超出大小限制、语法或结构非法时抛出
     */
    @Override
    public PluginManifest parse(InputStream input) {
        if (input == null) {
            throw invalidStructure("YAML input is required", null);
        }
        try {
            // 先按字节上限读取，避免不受控的流把解析器拖入大文档攻击面。
            byte[] bytes = input.readNBytes(MAX_DOCUMENT_BYTES + 1);
            if (bytes.length > MAX_DOCUMENT_BYTES) {
                throw invalidStructure("plugin YAML exceeds 1 MiB", null);
            }
            String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            // YAML 锚点/别名会引入共享可变图，与不可变 DSL 模型不兼容，因此在词法层直接拒绝。
            if (YAML_EXTENSION.matcher(text).find()) {
                throw invalidSyntax("YAML anchors, aliases and custom tags are not supported", null);
            }
            try (JsonParser parser = mapper.getFactory().createParser(bytes)) {
                PluginManifest manifest = mapper.readValue(parser, PluginManifest.class);
                if (manifest == null) {
                    throw invalidStructure("plugin YAML must not be null", null);
                }
                // 单文档约束防止多插件拼装在同一个资源里绕过发现边界。
                if (parser.nextToken() != null) {
                    throw invalidSyntax("plugin YAML must contain exactly one document", null);
                }
                return manifest;
            }
        } catch (NexusException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw invalidSyntax("cannot parse plugin YAML", exception);
        }
    }

    private static NexusException invalidSyntax(String message, Throwable cause) {
        return cause == null
                ? NexusException.build(PluginStatusCode.DSL_SYNTAX_INVALID, message)
                : NexusException.build(PluginStatusCode.DSL_SYNTAX_INVALID.fullCode(), message, cause);
    }

    private static NexusException invalidStructure(String message, Throwable cause) {
        return cause == null
                ? NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID, message)
                : NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID.fullCode(), message, cause);
    }
}
