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
 * 在运行时动作注册表中注册的一次动作调用。
 *
 * <p>当 {@link #id} 存在时，动作结果在同一作用域的后续步骤中可通过
 * {@code ${actions.id}} 引用。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionConfig {

    /** 可选结果名称，供后续表达式引用。 */
    private String id;

    /** 动作注册表名称，例如 {@code reload}、{@code setState} 或 {@code call}。 */
    private String action;

    /** 动作特定参数；结构取决于已注册的动作。 */
    private Map<String, Object> params = new LinkedHashMap<>();

    /** 可选执行守卫；接受布尔字面量或表达式字符串。 */
    @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
    private Object condition;

    /** 可选动作级权限。 */
    private PermissionConfig permission;
}
