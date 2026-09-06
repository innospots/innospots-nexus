package com.innospots.nexus.base.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes one action or an action array into {@link ActionOrList}.
 */
public final class ActionOrListDeserializer extends JsonDeserializer<ActionOrList> {

    @Override
    public ActionOrList deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node == null || node.isNull()) {
            return new ActionOrList(List.of());
        }
        if (node.isArray()) {
            List<ActionConfig> actions = new ArrayList<>();
            for (JsonNode item : node) {
                actions.add(mapper.treeToValue(item, ActionConfig.class));
            }
            return new ActionOrList(actions);
        }
        return new ActionOrList(List.of(mapper.treeToValue(node, ActionConfig.class)));
    }
}
