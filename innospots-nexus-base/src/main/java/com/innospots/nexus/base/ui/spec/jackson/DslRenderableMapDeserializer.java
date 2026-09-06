package com.innospots.nexus.base.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.ui.spec.node.DslRenderable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializes a map of named renderable components.
 */
public final class DslRenderableMapDeserializer extends JsonDeserializer<Map<String, DslRenderable>> {

    @Override
    public Map<String, DslRenderable> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        Map<String, DslRenderable> components = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return components;
        }
        DslRenderableDeserializer renderableDeserializer = new DslRenderableDeserializer();
        node.fields().forEachRemaining(entry -> {
            try {
                JsonParser childParser = entry.getValue().traverse(mapper);
                childParser.nextToken();
                components.put(entry.getKey(), renderableDeserializer.deserialize(childParser, context));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot deserialize component: " + entry.getKey(), exception);
            }
        });
        return components;
    }
}
