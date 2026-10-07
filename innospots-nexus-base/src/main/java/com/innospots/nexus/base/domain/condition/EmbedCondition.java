package com.innospots.nexus.base.domain.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 支持嵌套的条件——每个 {@link EmbedCondition} 可递归包含子条件。最终语句将嵌套组包裹在括号中。
 *
 * @author Smars
 * @date 2026/09/13
 * @see SimpleCondition
 */
public class EmbedCondition extends SimpleCondition {

    private final List<EmbedCondition> embeds = new ArrayList<>();

    private EmbedCondition(Mode mode, Relation relation) {
        super(mode, relation);
    }

    /**
     * 创建支持嵌套的可嵌入条件。
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
     * 添加嵌套子条件。重建时，嵌入条件被包裹
     * 在括号中并由父关系连接。
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

        // 在括号中追加嵌套条件：parent(cond1 AND cond2)
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
