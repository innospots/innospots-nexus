package com.innospots.nexus.base.ui;

import com.innospots.nexus.base.domain.condition.Factor;
import com.innospots.nexus.base.domain.condition.Relation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A visibility/enablement condition for a UI component. When the factors
 * are satisfied (according to the relation), the condition evaluates
 * to the specified result.
 */
@Getter
@Setter
public class UiCondition {

    private String result;
    private Relation relation;
    private List<Factor> factors = List.of();

    public static UiCondition when(Relation relation, List<Factor> factors, String result) {
        UiCondition condition = new UiCondition();
        condition.relation = relation;
        condition.factors = factors == null ? List.of() : List.copyOf(factors);
        condition.result = result;
        return condition;
    }

    public String result() {
        return result;
    }

    public Relation relation() {
        return relation;
    }

    public List<Factor> factors() {
        return factors;
    }
}
