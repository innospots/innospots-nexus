package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.Children;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslRenderable;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 YAML 数组或单个
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef}
 * 对象反序列化
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.Children}。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ChildrenDeserializer extends JsonDeserializer<Children> {

    @Override
    public Children deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node == null || node.isNull()) {
            return Children.ofItems(List.of());
        }
        if (node.isArray()) {
            List<DslRenderable> items = new ArrayList<>();
            for (JsonNode item : node) {
                items.add(readRenderable(mapper, item));
            }
            return Children.ofItems(items);
        }
        if (node.has("source")) {
            return Children.ofSourceRef(mapper.treeToValue(node, DslSourceRef.class));
        }
        return Children.ofItems(List.of(readRenderable(mapper, node)));
    }

    /**
     * 使用 Pactor schema 的结构启发式解析一个可渲染节点。
     *
     * <p>检测顺序：{@code source} → {@code component} → {@code type}。</p>
     *
     * @param mapper Jackson 对象映射器
     * @param node JSON 节点
     * @return 可渲染 DSL 片段
     * @throws IOException JSON 解析失败时
     */
    static DslRenderable readRenderable(ObjectMapper mapper, JsonNode node) throws IOException {
        if (node.has("source")) {
            return mapper.treeToValue(node, DslSourceRef.class);
        }
        if (node.has("component")) {
            return mapper.treeToValue(node, com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode.class);
        }
        return mapper.treeToValue(node, com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode.class);
    }
}
