package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.UiSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered chain of {@link UiSpecFilter} instances. Filters run sequentially;
 * each step receives the output of the previous filter as the working
 * specification.
 */
public final class UiSpecFilterChain {

    private final List<UiSpecFilter> filters;

    private UiSpecFilterChain(List<UiSpecFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    /** Creates an empty filter chain. */
    public static UiSpecFilterChain create() {
        return new UiSpecFilterChain(List.of());
    }

    /**
     * Creates a chain from the supplied filters in declaration order.
     *
     * @param filters ordered filters
     * @return filter chain
     */
    public static UiSpecFilterChain of(UiSpecFilter... filters) {
        if (filters == null || filters.length == 0) {
            return create();
        }
        List<UiSpecFilter> actual = new ArrayList<>(filters.length);
        for (UiSpecFilter filter : filters) {
            if (filter != null) {
                actual.add(filter);
            }
        }
        return new UiSpecFilterChain(actual);
    }

    /**
     * Returns a new chain with one additional trailing filter.
     *
     * @param filter filter to append
     * @return new chain containing all previous filters plus the new one
     */
    public UiSpecFilterChain add(UiSpecFilter filter) {
        if (filter == null) {
            return this;
        }
        List<UiSpecFilter> next = new ArrayList<>(filters.size() + 1);
        next.addAll(filters);
        next.add(filter);
        return new UiSpecFilterChain(next);
    }

    /**
     * Runs all configured filters and returns the final specification for
     * frontend rendering.
     *
     * @param context render context
     * @return processed specification
     */
    public UiSpec process(UiSpecRenderContext context) {
        if (context == null || context.spec() == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec render context is required");
        }
        UiSpec current = context.spec();
        if (filters.isEmpty()) {
            return current;
        }
        for (UiSpecFilter filter : filters) {
            UiSpec next = filter.filter(context.withSpec(current));
            if (next == null) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "UiSpec filter returned null: " + filter.filterId());
            }
            current = next;
        }
        return current;
    }

    /** Returns an immutable view of configured filters. */
    public List<UiSpecFilter> filters() {
        return filters;
    }
}
