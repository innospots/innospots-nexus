package com.innospots.nexus.console.ui.spec.loader;

import com.innospots.nexus.console.ui.spec.PageDsl;

/**
 * 从后端存储加载页面 DSL 文档。
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PageDslLoader {

    /**
     * 加载一个页面 DSL 文档。
     *
     * @param domainKey 项目领域键
     * @param moduleKey 所属模块键
     * @param pageKey 与 {@code page.id} 匹配的页面键
     * @return 页面 DSL 文档
     */
    PageDsl load(String domainKey, String moduleKey, String pageKey);
}
