package com.innospots.nexus.base.domain.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 由单一 {@link Relation} 连接的 {@link Factor} 平面列表。条件在首次通过 {@link #statement()} 访问时渲染为语句字符串并缓存，直至因子变更。
 *
 * @author Smars
 * @date 2026/09/13
 * @see EmbedCondition
 */
public class SimpleCondition {

    protected final Mode mode;
    protected final Relation relation;
    protected final List<Factor> factors = new ArrayList<>();
    protected String displayName;
    protected String statement;
    protected boolean initialized;

    protected SimpleCondition(Mode mode, Relation relation) {
        this.mode = mode;
        this.relation = relation;
    }

    /**
     * 使用指定输出模式与逻辑关系创建条件。
     */
    public static SimpleCondition create(Mode mode, Relation relation) {
        return new SimpleCondition(mode, relation);
    }

    public Mode mode() {
        return mode;
    }

    public Relation relation() {
        return relation;
    }

    public List<Factor> factors() {
        return List.copyOf(factors);
    }

    /**
     * 添加因子并使缓存的语句失效。
     */
    public SimpleCondition factor(Factor factor) {
        if (factor != null) {
            factors.add(factor);
            initialized = false;  // 下次 statement() 调用时强制重新生成
        }
        return this;
    }

    public String displayName() {
        return displayName;
    }

    public SimpleCondition displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * 返回渲染后的条件语句，必要时延迟生成。
     */
    public String statement() {
        if (!initialized) {
            initialize();
        }
        return statement;
    }

    /**
     * 根据当前模式初始化合适的 {@link IFactorStatement} 并渲染条件语句。
     */
    public void initialize() {
        ensureReady();
        IFactorStatement fs = FactorStatementBuilder.build(mode);
        this.statement = rebuild(fs).toString();
        initialized = true;
    }

    /**
     * 校验条件至少有一个因子且关系非 null。
     */
    protected void ensureReady() {
        if (factors.isEmpty()) {
            throw new IllegalStateException("condition factors is empty");
        }
        if (relation == null) {
            throw new IllegalStateException("condition relation is null");
        }
    }

    /**
     * 使用关系符号连接因子语句。
     * 单因子条件省略关系连接符。
     */
    protected StringBuilder rebuild(IFactorStatement fs) {
        StringBuilder buf = new StringBuilder();
        if (factors.size() == 1) {
            buf.append(fs.statement(factors.get(0)));
        } else {
            for (int i = 0; i < factors.size(); i++) {
                buf.append(fs.statement(factors.get(i)));
                if (i < factors.size() - 1) {
                    buf.append(relation.symbol(mode));
                }
            }
        }
        return buf;
    }

    /**
     * 将另一条件的因子合并到当前条件，并使
     * 缓存的语句失效。
     */
    public void merge(SimpleCondition other) {
        if (other != null && !other.factors.isEmpty()) {
            this.factors.addAll(other.factors);
            initialized = false;
        }
    }

    /**
     * 收集此条件因子引用的所有字段编码。
     */
    public Set<String> referFields() {
        Set<String> fields = new LinkedHashSet<>();
        for (Factor factor : factors) {
            if (factor != null && factor.code() != null) {
                fields.add(factor.code());
            }
        }
        return Set.copyOf(fields);
    }

    @Override
    public String toString() {
        return "{" +
                "factors=" + factors +
                ", relation=" + relation +
                ", mode=" + mode +
                ", statement='" + statement + '\'' +
                '}';
    }
}
