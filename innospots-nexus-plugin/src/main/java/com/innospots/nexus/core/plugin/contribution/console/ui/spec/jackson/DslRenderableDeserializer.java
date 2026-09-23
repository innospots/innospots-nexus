package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslRenderable;

import java.io.IOException;

/**
 * 反序列化一个可渲染 DSL 片段。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class DslRenderableDeserializer extends JsonDeserializer<DslRenderable> {

    @Override
    public DslRenderable deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        return ChildrenDeserializer.readRenderable(mapper, node);
    }
}
