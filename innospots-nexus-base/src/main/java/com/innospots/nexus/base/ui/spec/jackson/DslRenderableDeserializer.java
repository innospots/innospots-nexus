package com.innospots.nexus.base.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.ui.spec.node.DslRenderable;

import java.io.IOException;

/**
 * Deserializes one renderable DSL fragment.
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
