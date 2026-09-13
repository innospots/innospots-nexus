package com.innospots.nexus.core.plugin.contribution.console.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ExpressionOrBooleanDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * One action invocation registered in the runtime action registry.
 *
 * <p>When {@link #id} is present, the action result is exposed as
 * {@code ${actions.id}} to subsequent steps in the same scope.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionConfig {

    /** Optional result name for later expression references. */
    private String id;

    /** Action registry name, for example {@code reload}, {@code setState}, or {@code call}. */
    private String action;

    /** Action-specific parameters. Structure depends on the registered action. */
    private Map<String, Object> params = new LinkedHashMap<>();

    /** Optional execution guard. Accepts a boolean literal or expression string. */
    @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
    private Object condition;

    /** Optional action-level permission. */
    private PermissionConfig permission;
}
