package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将单个动作或动作数组反序列化为 {@link ActionOrList}。
 *
 * @author Smars
 * @date 2026/09/13
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
