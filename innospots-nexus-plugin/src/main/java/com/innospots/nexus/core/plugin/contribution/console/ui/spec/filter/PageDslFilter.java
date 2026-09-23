package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/**
 * 在渲染时准备阶段转换页面 DSL 文档。
 *
 * <p>实现必须返回非 null 文档。当转换限定于当前请求时，过滤器可就地修改工作文档。</p>
 *
 * @see PageDslFilterChain
 * @see PageDslRenderContext
 * @author Smars
 * @date 2026/09/13
 */
@FunctionalInterface
public interface PageDslFilter {

    /**
     * 应用一步转换，并返回供下一过滤器使用的文档。
     *
     * @param context 当前渲染上下文
     * @return 转换后的文档；不得为 {@code null}
     */
    PageDsl filter(PageDslRenderContext context);

    /**
     * 返回用于日志与诊断的稳定标识符。
     *
     * @return 过滤器标识符
     */
    default String filterId() {
        return getClass().getSimpleName();
    }
}
