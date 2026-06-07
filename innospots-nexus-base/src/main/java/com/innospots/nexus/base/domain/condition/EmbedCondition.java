package com.innospots.nexus.base.domain.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A condition that supports nesting — each {@link EmbedCondition} can contain
 * child sub-conditions recursively. The final statement wraps nested groups
 * in parentheses.
 */
public class EmbedCondition extends SimpleCondition {

    private final List<EmbedCondition> embeds = new ArrayList<>();

    private EmbedCondition(Mode mode, Relation relation) {
        super(mode, relation);
    }

    /**
     * Creates an embeddable condition with nesting support.
     */
    public static EmbedCondition create(Mode mode, Relation relation) {
        return new EmbedCondition(mode, relation);
    }

    @Override
    public EmbedCondition factor(Factor factor) {
        super.factor(factor);
        return this;
    }

    public List<EmbedCondition> embeds() {
        return List.copyOf(embeds);
    }

    /**
     * Adds a nested sub-condition. On rebuild, embedded conditions are wrapped
     * in parentheses and joined by the parent relation.
     */
    public EmbedCondition addCondition(EmbedCondition condition) {
        if (condition != null) {
            embeds.add(condition);
            initialized = false;
        }
        return this;
    }

    @Override
    public void merge(SimpleCondition other) {
        super.merge(other);
        if (other instanceof EmbedCondition eo && !eo.embeds.isEmpty()) {
            embeds.addAll(eo.embeds);
        }
    }

    @Override
    protected StringBuilder rebuild(IFactorStatement fs) {
        StringBuilder buf = super.rebuild(fs);

        // Append nested conditions in parentheses: parent(cond1 AND cond2)
        if (!embeds.isEmpty()) {
            boolean brace = buf.length() > 0;
            if (brace) {
                buf.append("(");
            }
            for (int i = 0; i < embeds.size(); i++) {
                EmbedCondition cond = embeds.get(i);
                cond.initialize();
                if (cond.statement != null && !cond.statement.isBlank()) {
                    buf.append("(").append(cond.statement).append(")");
                }
                if (i < embeds.size() - 1) {
                    buf.append(relation.symbol(mode));
                }
            }
            if (brace) {
                buf.append(")");
            }
        }

        return buf;
    }

    @Override
    public Set<String> referFields() {
        Set<String> fields = new LinkedHashSet<>(super.referFields());
        for (EmbedCondition cond : embeds) {
            if (cond != null) {
                fields.addAll(cond.referFields());
            }
        }
        return Set.copyOf(fields);
    }
}
