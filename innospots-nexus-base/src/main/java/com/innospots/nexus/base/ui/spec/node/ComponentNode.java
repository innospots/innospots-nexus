package com.innospots.nexus.base.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.base.ui.spec.jackson.ChildrenDeserializer;
import com.innospots.nexus.base.ui.spec.jackson.EventMapDeserializer;
import com.innospots.nexus.base.ui.spec.jackson.ExpressionOrBooleanDeserializer;
import com.innospots.nexus.base.ui.spec.permission.PermissionConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Inline component node declared with a registry {@code type}.
 *
 * <p>{@code type} and {@code component} are mutually exclusive node shapes. Component-specific
 * {@link #props} are intentionally open and validated by the component registry.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ComponentNode implements DslNode {

    private String type;
    private String id;
    private Map<String, Object> props = new LinkedHashMap<>();

    @JsonDeserialize(using = ChildrenDeserializer.class)
    private Children children;

    @JsonDeserialize(using = EventMapDeserializer.class)
    private Map<String, com.innospots.nexus.base.ui.spec.action.ActionOrList> events = new LinkedHashMap<>();

    private PermissionConfig permission;

    @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
    private Object when;
}
