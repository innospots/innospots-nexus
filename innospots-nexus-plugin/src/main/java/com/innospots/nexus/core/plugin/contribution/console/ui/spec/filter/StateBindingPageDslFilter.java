package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/**
 * 将请求参数绑定到页面 DSL 的 {@code state} 映射。
 *
 * <p>请求参数覆盖已有状态键，因渲染时过滤器通常在文档从存储加载后执行。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class StateBindingPageDslFilter implements PageDslFilter {

    /** 创建状态绑定过滤器。 */
    public StateBindingPageDslFilter() {
    }

    @Override
    public PageDsl filter(PageDslRenderContext context) {
        PageDsl document = context.document();
        document.bindState(context.parameters());
        return document;
    }
}
