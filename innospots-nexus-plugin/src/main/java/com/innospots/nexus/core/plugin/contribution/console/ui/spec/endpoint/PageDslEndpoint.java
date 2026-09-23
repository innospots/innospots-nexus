package com.innospots.nexus.core.plugin.contribution.console.ui.spec.endpoint;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

import java.util.Map;

/**
 * 加载与准备页面 DSL 文档的渲染时 API。
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PageDslEndpoint {

    /**
     * 加载并准备一个页面 DSL 文档。
     *
     * @param moduleKey 所属模块键
     * @param pageKey 与 {@code page.id} 匹配的页面键
     * @param parameters 运行时请求参数
     * @return 已准备的页面 DSL 文档
     */
    PageDsl render(String moduleKey, String pageKey, Map<String, Object> parameters);
}
