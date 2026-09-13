package com.innospots.nexus.core.plugin.contribution.console.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ActionOrListDeserializer;

import java.util.List;

/**
 * One action or an ordered action sequence.
 *
 * <p>YAML may declare either a single action object or an array. Event handlers, lifecycle
 * hooks, and page-level {@code actions} entries all use this union type.</p>
 *
 * @param actions ordered action steps; never {@code null}
 */
@JsonDeserialize(using = ActionOrListDeserializer.class)
public record ActionOrList(List<ActionConfig> actions) {

    /**
     * Creates an action list wrapper.
     *
     * @param actions ordered actions
     */
    public ActionOrList {
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    /**
     * Returns whether the wrapper contains any actions.
     *
     * @return true when at least one action is present
     */
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * Serializes as a single action object or an action array, matching the YAML union type.
     *
     * @return action steps
     */
    @JsonValue
    public List<ActionConfig> toJsonValue() {
        return actions;
    }
}
