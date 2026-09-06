package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.PageDsl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered chain of {@link PageDslFilter} instances.
 *
 * <p>Filters run sequentially. Each step receives the output of the previous filter as the
 * working document through {@link PageDslRenderContext#withDocument(PageDsl)}.</p>
 */
public final class PageDslFilterChain {

    private final List<PageDslFilter> filters;

    private PageDslFilterChain(List<PageDslFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    /** Creates an empty filter chain. */
    public static PageDslFilterChain create() {
        return new PageDslFilterChain(List.of());
    }

    /**
     * Creates a filter chain from the supplied filters.
     *
     * @param filters ordered filters
     * @return filter chain
     */
    public static PageDslFilterChain of(PageDslFilter... filters) {
        if (filters == null || filters.length == 0) {
            return create();
        }
        List<PageDslFilter> actual = new ArrayList<>(filters.length);
        for (PageDslFilter filter : filters) {
            if (filter != null) {
                actual.add(filter);
            }
        }
        return new PageDslFilterChain(actual);
    }

    /**
     * Returns a new chain with one additional filter appended.
     *
     * @param filter filter to append
     * @return new filter chain
     */
    public PageDslFilterChain add(PageDslFilter filter) {
        if (filter == null) {
            return this;
        }
        List<PageDslFilter> next = new ArrayList<>(filters.size() + 1);
        next.addAll(filters);
        next.add(filter);
        return new PageDslFilterChain(next);
    }

    /**
     * Runs the configured filters against the supplied context.
     *
     * @param context render context
     * @return processed page DSL
     */
    public PageDsl process(PageDslRenderContext context) {
        if (context == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl render context is required");
        }
        PageDsl current = context.document();
        if (current == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl render context requires a document");
        }
        for (PageDslFilter filter : filters) {
            PageDsl next = filter.filter(context.withDocument(current));
            if (next == null) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "PageDsl filter returned null: " + filter.filterId());
            }
            current = next;
        }
        return current;
    }

    /** Returns an immutable view of configured filters. */
    public List<PageDslFilter> filters() {
        return filters;
    }
}
