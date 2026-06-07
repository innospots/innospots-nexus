package com.innospots.nexus.base.domain.condition;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionContractsTest {

    @Test
    void groupsFactorsAndBuildsDbStatement() {
        SimpleCondition condition = SimpleCondition.create(Mode.DB, Relation.AND)
                .factor(Factor.of("age", Operator.GREATER_EQUAL, 18))
                .factor(Factor.of("country", Operator.IN, List.of("CN", "US")))
                .displayName("adult audience");

        assertThat(condition.relation()).isEqualTo(Relation.AND);
        assertThat(condition.referFields()).isEqualTo(Set.of("age", "country"));
        assertThat(condition.factors()).hasSize(2);
        assertThat(condition.displayName()).isEqualTo("adult audience");
        assertThat(condition.statement()).contains("age >= 18");
        assertThat(condition.statement()).contains("country IN");
    }

    @Test
    void handlesBetweenInDbStatement() {
        SimpleCondition condition = SimpleCondition.create(Mode.DB, Relation.AND)
                .factor(Factor.of("score", Operator.BETWEEN, List.of(80, 100)));

        assertThat(condition.statement()).contains("score BETWEEN 80 AND 100");
        assertThat(Operator.BETWEEN.isRange()).isTrue();
    }

    @Test
    void embedsNestedConditionsWithParentheses() {
        EmbedCondition parent = EmbedCondition.create(Mode.DB, Relation.OR)
                .factor(Factor.of("enabled", Operator.EQUAL, true));

        EmbedCondition child = EmbedCondition.create(Mode.DB, Relation.AND)
                .factor(Factor.of("score", Operator.GREATER_EQUAL, 80))
                .factor(Factor.of("score", Operator.LESS_EQUAL, 100));

        parent.addCondition(child);

        String stmt = parent.statement();
        assertThat(stmt).contains("enabled = true");
        assertThat(stmt).contains("score >= 80");
        assertThat(stmt).contains("score <= 100");
        assertThat(parent.referFields()).contains("enabled", "score");
    }

    @Test
    void supportsScriptModeStatement() {
        SimpleCondition condition = SimpleCondition.create(Mode.SCRIPT, Relation.AND)
                .factor(Factor.of("name", Operator.EQUAL, "nexus"));

        assertThat(condition.statement()).contains("name").contains("==").contains("'nexus'");
    }

    @Test
    void factorSupportsPlaceholderResolution() {
        Factor factor = Factor.of("userName", Operator.EQUAL, "${userName}");

        assertThat(factor.valueKey()).isEqualTo("userName");
        assertThat(factor.value(java.util.Map.of("userName", "admin"))).isEqualTo("admin");
    }
}
