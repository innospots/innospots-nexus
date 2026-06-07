package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class VisibleCondition {

    private String expression;
    private Map<String, Object> context = new LinkedHashMap<>();

    public VisibleCondition() {
    }

    public static VisibleCondition expression(String expression) {
        VisibleCondition condition = new VisibleCondition();
        condition.expression = expression;
        return condition;
    }

    public String expression() {
        return expression;
    }

    public Map<String, Object> context() {
        return Map.copyOf(context);
    }

    public VisibleCondition context(String key, Object value) {
        if (key != null) {
            context.put(key, value);
        }
        return this;
    }
}
