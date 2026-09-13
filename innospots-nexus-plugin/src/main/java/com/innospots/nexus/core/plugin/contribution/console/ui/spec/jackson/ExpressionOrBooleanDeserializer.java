package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Deserializes expression strings or boolean literals.
 */
public final class ExpressionOrBooleanDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.asText();
    }
}
