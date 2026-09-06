package com.innospots.nexus.base.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializes a map of named actions.
 */
public final class ActionOrListMapDeserializer extends JsonDeserializer<Map<String, ActionOrList>> {

    @Override
    public Map<String, ActionOrList> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        Map<String, ActionOrList> actions = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return actions;
        }
        ActionOrListDeserializer actionDeserializer = new ActionOrListDeserializer();
        node.fields().forEachRemaining(entry -> {
            try {
                JsonParser childParser = entry.getValue().traverse(mapper);
                childParser.nextToken();
                actions.put(entry.getKey(), actionDeserializer.deserialize(childParser, context));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot deserialize action: " + entry.getKey(), exception);
            }
        });
        return actions;
    }
}
