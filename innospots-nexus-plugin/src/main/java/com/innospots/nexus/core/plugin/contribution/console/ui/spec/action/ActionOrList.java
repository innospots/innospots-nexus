package com.innospots.nexus.core.plugin.contribution.console.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ActionOrListDeserializer;

import java.util.List;

/**
 * 单个动作或有序动作序列。
 *
 * <p>YAML 可声明单个动作对象或数组。事件处理器、生命周期钩子与页面级 {@code actions}
 * 条目均使用此联合类型。</p>
 *
 * @param actions 有序动作步骤；永不为 {@code null}
 * @author Smars
 * @date 2026/09/13
 */
@JsonDeserialize(using = ActionOrListDeserializer.class)
public record ActionOrList(List<ActionConfig> actions) {

    /**
     * 创建动作列表包装器。
     *
     * @param actions 有序动作列表
     */
    public ActionOrList {
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    /**
     * 返回包装器是否包含任何动作。
     *
     * @return 存在至少一个动作时返回 {@code true}
     */
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * 序列化为单个动作对象或动作数组，与 YAML 联合类型一致。
     *
     * @return 动作步骤列表
     */
    @JsonValue
    public List<ActionConfig> toJsonValue() {
        return actions;
    }
}
