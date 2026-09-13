package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializes component event maps.
 */
public final class EventMapDeserializer extends JsonDeserializer<Map<String, ActionOrList>> {

    @Override
    public Map<String, ActionOrList> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        Map<String, ActionOrList> events = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return events;
        }
        ActionOrListDeserializer actionDeserializer = new ActionOrListDeserializer();
        node.fields().forEachRemaining(entry -> {
            try {
                JsonParser childParser = entry.getValue().traverse(mapper);
                childParser.nextToken();
                events.put(entry.getKey(), actionDeserializer.deserialize(childParser, context));
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot deserialize event: " + entry.getKey(), exception);
            }
        });
        return events;
    }
}
