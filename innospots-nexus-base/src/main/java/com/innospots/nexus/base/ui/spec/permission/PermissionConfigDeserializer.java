package com.innospots.nexus.base.ui.spec.permission;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deserializes {@link PermissionConfig} from string, array, or object forms.
 */
public final class PermissionConfigDeserializer extends JsonDeserializer<PermissionConfig> {

    @Override
    public PermissionConfig deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return PermissionConfig.fromCode(node.asText());
        }
        if (node.isArray()) {
            List<String> codes = new ArrayList<>();
            Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                JsonNode item = iterator.next();
                if (item != null && item.isTextual()) {
                    codes.add(item.asText());
                }
            }
            return PermissionConfig.fromAnyOf(codes);
        }
        if (node.isObject()) {
            JsonNode codeNode = node.get("code");
            String code = codeNode == null || codeNode.isNull() ? null : codeNode.asText();
            PermissionDenied denied = PermissionDenied.HIDDEN;
            JsonNode deniedNode = node.get("denied");
            if (deniedNode != null && deniedNode.isTextual()) {
                denied = PermissionDenied.valueOf(deniedNode.asText().toUpperCase());
            }
            return PermissionConfig.fromDetailed(code, denied);
        }
        return null;
    }
}
