package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 有序的 {@link PageDslFilter} 实例链。
 *
 * <p>过滤器按序执行。每一步通过 {@link PageDslRenderContext#withDocument(PageDsl)}
 * 接收上一过滤器的输出作为工作文档。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PageDslFilterChain {

    private final List<PageDslFilter> filters;

    private PageDslFilterChain(List<PageDslFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    /**
     * 创建空过滤器链。
     *
     * @return 空过滤器链
     */
    public static PageDslFilterChain create() {
        return new PageDslFilterChain(List.of());
    }

    /**
     * 根据提供的过滤器创建过滤器链。
     *
     * @param filters 有序过滤器
     * @return 过滤器链
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
     * 返回追加了新过滤器的新链。
     *
     * @param filter 待追加的过滤器
     * @return 新过滤器链
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
     * 对提供的上下文执行已配置的过滤器。
     *
     * @param context 渲染上下文
     * @return 处理后的页面 DSL
     * @throws com.innospots.nexus.base.exception.NexusException 上下文无效或过滤器返回 null 时
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

    /**
     * 返回已配置过滤器的不可变视图。
     *
     * @return 过滤器列表
     */
    public List<PageDslFilter> filters() {
        return filters;
    }
}
