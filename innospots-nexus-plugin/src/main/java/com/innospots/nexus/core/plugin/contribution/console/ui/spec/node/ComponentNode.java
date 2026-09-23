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
 * 以注册表 {@code type} 声明的内联组件节点。
 *
 * <p>{@code type} 与 {@code component} 为互斥节点形态。组件特定的 {@link #props}
 * 有意保持开放，由组件注册表校验。</p>
 *
 * @author Smars
 * @date 2026/09/13
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
    private Map<String, com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList> events = new LinkedHashMap<>();

    private PermissionConfig permission;

    @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
    private Object when;
}
