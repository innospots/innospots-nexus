package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslRenderable;

import java.io.IOException;

/**
 * Deserializes {@link DslNode} instances.
 */
public final class DslNodeDeserializer extends JsonDeserializer<DslNode> {

    @Override
    public DslNode deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        DslRenderable renderable = ChildrenDeserializer.readRenderable(mapper, node);
        if (renderable instanceof DslNode dslNode) {
            return dslNode;
        }
        return null;
    }
}
