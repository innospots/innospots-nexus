package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ChildrenDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.EventMapDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ExpressionOrBooleanDeserializer;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Reference to a named component declared in {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl#components}.
 *
 * <p>May optionally override {@link #props}, {@link #events}, or {@link #children} for the
 * referenced component instance.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ComponentReferenceNode implements DslNode {

    private String component;
    private String id;
    private Map<String, Object> props = new LinkedHashMap<>();

    @JsonDeserialize(using = ChildrenDeserializer.class)
    private Children children;

    @JsonDeserialize(using = EventMapDeserializer.class)
    private Map<String, com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList> events = new LinkedHashMap<>();

    private PermissionConfig permission;

    @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
    private Object when;
}
