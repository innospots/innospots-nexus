package com.innospots.nexus.base.domain.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A flat list of {@link Factor} conditions joined by a single {@link Relation}.
 * The condition renders itself as a statement string on first access via
 * {@link #statement()}, caching the result until factors change.
 *
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
     * Creates a condition with the specified output mode and logical relation.
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
     * Adds a factor and invalidates the cached statement.
     */
    public SimpleCondition factor(Factor factor) {
        if (factor != null) {
            factors.add(factor);
            initialized = false;  // force re-generation on next statement() call
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
     * Returns the rendered condition statement, generating it lazily if needed.
     */
    public String statement() {
        if (!initialized) {
            initialize();
        }
        return statement;
    }

    /**
     * Generates the condition statement using the appropriate
     * {@link IFactorStatement} for the current mode.
     */
    public void initialize() {
        ensureReady();
        IFactorStatement fs = FactorStatementBuilder.build(mode);
        this.statement = rebuild(fs).toString();
        initialized = true;
    }

    /**
     * Validates that the condition has at least one factor and a non-null relation.
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
     * Joins factor statements using the relation symbol.
     * Single-factor conditions omit the relation connector.
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
     * Merges the factors from another condition into this one, invalidating
     * the cached statement.
     */
    public void merge(SimpleCondition other) {
        if (other != null && !other.factors.isEmpty()) {
            this.factors.addAll(other.factors);
            initialized = false;
        }
    }

    /**
     * Collects all field codes referenced by this condition's factors.
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
